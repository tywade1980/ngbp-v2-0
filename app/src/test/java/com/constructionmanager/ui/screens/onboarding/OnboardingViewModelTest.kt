package com.constructionmanager.ui.screens.onboarding

import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.CarolineEndpoint
import com.constructionmanager.data.caroline.ConnectorError
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.caroline.LoginResponse
import com.constructionmanager.data.caroline.PairResponse
import com.constructionmanager.data.caroline.SetPasswordResponse
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
 * The front door's ways in, pinned down:
 *  - the sign-in path ("I already have a Caroline") on a fresh install binds
 *    this build's own Caroline endpoint and works against the owner's
 *    EXISTING system (the 1.2 dead-end: pair() refused, unbound, forever);
 *    since Tyler's ruling (Oct 26 2026) that path opens on EMAIL + PASSWORD
 *    — the one universal door — with the one-time-code pane as the alternate;
 *  - the subscriber launch flow still binds the TENANT office from the
 *    launch confirmation, and neither pair() nor signIn() ever overrides it
 *    with the default.
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

    private val urlAtSignIn = AtomicReference<String?>(null)
    private val signInArgs = AtomicReference<List<String?>>(null)
    private val setupArgs = AtomicReference<List<String?>>(null)

    /** The connector's sign-in succeeds; records the base URL and the exact words it was handed. */
    private fun stubSignInSuccess() {
        runBlocking {
            Mockito.`when`(
                connector.signIn(
                    ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                    ArgumentMatchers.nullable(String::class.java), ArgumentMatchers.nullable(String::class.java),
                    ArgumentMatchers.anyString()
                )
            ).thenAnswer { inv ->
                urlAtSignIn.set(settings.baseUrl)
                signInArgs.set(listOf(inv.getArgument(0), inv.getArgument(1)))
                pairedFlag.set(true)
                ConnectorResult.Ok(LoginResponse(deviceId = "dev-1", token = "device-token", tenant = "wcc", label = "Owner's phone", email = inv.getArgument(0)))
            }
        }
    }

    private fun stubSignInRefused() {
        runBlocking {
            Mockito.`when`(
                connector.signIn(
                    ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                    ArgumentMatchers.nullable(String::class.java), ArgumentMatchers.nullable(String::class.java),
                    ArgumentMatchers.anyString()
                )
            ).thenAnswer {
                ConnectorResult.Failed(ConnectorError.Http(401, "wrong email or password; check both and try again"))
            }
        }
    }

    private fun stubSetPassword() {
        runBlocking {
            Mockito.`when`(
                connector.setPassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
            ).thenAnswer { inv ->
                urlAtSignIn.set(settings.baseUrl)
                setupArgs.set(listOf(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)))
                ConnectorResult.Ok(SetPasswordResponse(tenant = "wcc", email = inv.getArgument(0), passwordSet = true))
            }
        }
    }

    @Test
    fun `fresh install sign-in binds the default Caroline and lands on done`() {
        store.skipped = true
        val vm = viewModel()
        assertEquals(OnboardingPane.SIGNIN, vm.uiState.value.pane)

        stubSignInSuccess()
        vm.setSignInEmail("tyler@example.com")
        vm.setSignInPassword("correct horse battery")
        vm.signIn()

        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, store.boundOfficeUrl)
        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, urlAtSignIn.get())
        assertEquals(listOf("tyler@example.com", "correct horse battery"), signInArgs.get())
        assertTrue(vm.uiState.value.paired)
        assertEquals("", vm.uiState.value.signInPassword)   // the typed password is not kept
        assertEquals(OnboardingPane.DONE, vm.uiState.value.pane)
    }

    @Test
    fun `wrong password stays on the sign-in pane with the server's words and drops the typed password`() {
        store.skipped = true
        val vm = viewModel()
        stubSignInRefused()
        vm.setSignInEmail("tyler@example.com")
        vm.setSignInPassword("not it")
        vm.signIn()
        assertEquals(OnboardingPane.SIGNIN, vm.uiState.value.pane)
        assertFalse(vm.uiState.value.paired)
        assertEquals("", vm.uiState.value.signInPassword)
        assertTrue(vm.uiState.value.signInNote!!.contains("wrong email or password"))
    }

    @Test
    fun `sign-in without the sign-in choice and without a launch still waits for the launch`() {
        val vm = viewModel()
        vm.setSignInEmail("tyler@example.com")
        vm.setSignInPassword("correct horse battery")
        vm.signIn()
        assertEquals(
            "Your system is not bound to this phone yet — wait for the launch to finish.",
            vm.uiState.value.signInNote
        )
        assertNull(urlAtSignIn.get())
        assertFalse(vm.uiState.value.paired)
    }

    @Test
    fun `set password sends the email, the code and the chosen password, then clears them`() {
        store.skipped = true
        val vm = viewModel()
        stubSetPassword()
        vm.setSignInEmail("tyler@example.com")
        vm.setSettingUp(true)
        vm.setSetupCode("ABCD-EFGH")
        vm.setSetupPassword("a brand new password")
        vm.setPassword()
        assertEquals(listOf("tyler@example.com", "ABCD-EFGH", "a brand new password"), setupArgs.get())
        assertEquals(CarolineEndpoint.DEFAULT_BASE_URL, urlAtSignIn.get())
        assertEquals("", vm.uiState.value.setupCode)
        assertEquals("", vm.uiState.value.setupPassword)
        assertFalse(vm.uiState.value.settingUp)
        assertTrue(vm.uiState.value.signInNote!!.contains("Password set"))
    }

    @Test
    fun `sign-in choice opens the email and password pane instead of dismissing into the app`() {
        val vm = viewModel()
        vm.skip()
        assertTrue(store.skipped)
        assertEquals(OnboardingPane.SIGNIN, vm.uiState.value.pane)
        assertTrue(vm.uiState.value.signInPath)
        assertFalse(vm.uiState.value.dismissed)
    }

    @Test
    fun `from sign-in, the one-time code is the alternate door and back returns to sign-in`() {
        store.skipped = true
        val vm = viewModel()
        vm.goConnect()
        assertEquals(OnboardingPane.CONNECT, vm.uiState.value.pane)
        vm.backFromConnect()
        assertEquals(OnboardingPane.SIGNIN, vm.uiState.value.pane)
    }

    @Test
    fun `fresh install on the sign-in path pairs against the default Caroline`() {
        store.skipped = true // the persisted choice from "I already have a Caroline — sign in instead"
        val vm = viewModel()
        assertEquals(OnboardingPane.SIGNIN, vm.uiState.value.pane)
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

    @Test
    fun `sign-in after a launch uses the tenant office, never the default`() {
        store.launchToken = "tok-1"
        val done = LaunchStatus(
            token = "tok-1", slug = "tenant-co", status = "done", businessName = "Tenant Co",
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

        stubSignInSuccess()
        vm.goSignIn()
        vm.setSignInEmail("owner@tenant.example.com")
        vm.setSignInPassword("correct horse battery")
        vm.signIn()

        assertEquals("https://tenant.example.com/api/", urlAtSignIn.get())
        assertEquals("https://tenant.example.com/api/", store.boundOfficeUrl)
        assertTrue(vm.uiState.value.paired)
        assertEquals(OnboardingPane.DONE, vm.uiState.value.pane)
    }
}
