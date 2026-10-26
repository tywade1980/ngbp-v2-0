package com.constructionmanager.ui.screens.onboarding

import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.CarolineEndpoint
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.caroline.PairResponse
import com.constructionmanager.data.subscriber.LauncherClient
import com.constructionmanager.data.subscriber.LauncherResult
import com.constructionmanager.data.subscriber.LaunchConfirmation
import com.constructionmanager.data.subscriber.LaunchStatus
import com.constructionmanager.data.subscriber.SubscriberStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * The front door's two ways in, pinned down:
 *  - the owner sign-in path ("I already have a Caroline") on a fresh install
 *    binds this build's own Caroline endpoint and pairs against the owner's
 *    EXISTING system (the 1.2 dead-end: pair() refused, unbound, forever);
 *  - the subscriber launch flow still binds the TENANT office from the
 *    launch confirmation, and pair() never overrides it with the default.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private class FakeStore : SubscriberStore {
        override var launchToken: String? = null
        override var boundOfficeUrl: String? = null
        override var boundCrmUrl: String? = null
        override var businessName: String? = null
        override var businessNumber: String? = null
        override var launchCompleted: Boolean = false
        override var skipped: Boolean = false
    }

    private class FakeSettings : ConnectorSettings {
        override var baseUrl: String = CarolineEndpoint.DEFAULT_BASE_URL
        override var deviceToken: String? = null
        override var deviceId: String? = null
    }

    private lateinit var store: FakeStore
    private lateinit var settings: FakeSettings
    private lateinit var launcher: LauncherClient
    private lateinit var connector: CarolineConnector
    private val pairedFlag = AtomicBoolean(false)
    private val urlAtPair = AtomicReference<String?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        store = FakeStore()
        settings = FakeSettings()
        launcher = Mockito.mock(LauncherClient::class.java)
        connector = Mockito.mock(CarolineConnector::class.java)
        Mockito.`when`(connector.isPaired).thenAnswer { pairedFlag.get() }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = OnboardingViewModel(launcher, store, connector, settings)

    /** The connector's pair succeeds; records the base URL the VM had set at the moment of the call. */
    private fun stubPairSuccess() {
        runBlocking {
            Mockito.`when`(
                connector.pair(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.nullable(String::class.java),
                    ArgumentMatchers.nullable(String::class.java),
                    ArgumentMatchers.anyString()
                )
            ).thenAnswer {
                urlAtPair.set(settings.baseUrl)
                pairedFlag.set(true)
                ConnectorResult.Ok(PairResponse(deviceId = "dev-1", token = "device-token", tenant = "wcc", label = "Owner's phone"))
            }
        }
    }

    @Test
    fun `sign-in choice opens the pair pane instead of dismissing into the app`() {
        val vm = viewModel()
        vm.skip()
        assertTrue(store.skipped)
        assertEquals(OnboardingPane.CONNECT, vm.uiState.value.pane)
        assertFalse(vm.uiState.value.dismissed)
    }

    @Test
    fun `fresh install on the sign-in path pairs against the default Caroline`() {
        store.skipped = true // the persisted choice from "I already have a Caroline — sign in instead"
        val vm = viewModel()
        assertEquals(OnboardingPane.CONNECT, vm.uiState.value.pane)
        assertNull(store.boundOfficeUrl)

        stubPairSuccess()
        vm.setPairCode("654321")
        vm.pair()

        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, store.boundOfficeUrl)
        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, settings.baseUrl)
        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, urlAtPair.get())
        assertTrue(vm.uiState.value.paired)
        assertEquals(OnboardingPane.DONE, vm.uiState.value.pane)
    }

    @Test
    fun `unbound phone that never chose sign-in is still told to wait for its launch`() {
        val vm = viewModel()
        vm.setPairCode("654321")
        vm.pair()
        assertEquals(
            "Your system is not bound to this phone yet — wait for the launch to finish.",
            vm.uiState.value.pairNote
        )
        assertNull(store.boundOfficeUrl)
        assertNull(urlAtPair.get())
        assertFalse(vm.uiState.value.paired)
    }

    @Test
    fun `launch completion binds the tenant office and pair keeps that binding`() {
        store.launchToken = "tok-1"
        val done = LaunchStatus(
            token = "tok-1", slug = "acme", status = "done", businessName = "Acme",
            steps = emptyList(),
            confirmation = LaunchConfirmation(
                businessNumber = "6145550100", crmUrl = "https://tenant.example.com/app",
                appToken = null, voice = null, texting = null, facilitation = null, admin = null
            )
        )
        runBlocking {
            Mockito.`when`(launcher.status("tok-1")).thenReturn(LauncherResult.Ok(done))
        }
        val vm = viewModel()

        assertEquals("https://tenant.example.com/api/", store.boundOfficeUrl)
        assertEquals("https://tenant.example.com/api/", settings.baseUrl)
        assertTrue(store.launchCompleted)

        stubPairSuccess()
        vm.setPairCode("654321")
        vm.pair()

        assertEquals("https://tenant.example.com/api/", urlAtPair.get())
        assertEquals("https://tenant.example.com/api/", store.boundOfficeUrl)
        assertTrue(vm.uiState.value.paired)
    }
}
