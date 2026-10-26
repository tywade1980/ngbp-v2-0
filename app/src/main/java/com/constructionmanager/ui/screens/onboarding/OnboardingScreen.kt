package com.constructionmanager.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.constructionmanager.data.subscriber.LaunchStatus
import com.constructionmanager.data.subscriber.SubscriberIntake

/**
 * The subscriber front door: the whole turnkey flow in one screen —
 * welcome, the intake (the same facts the subscribe page collects, no
 * payment), the live launch watch, and the handoff to the tenant's own
 * Caroline on their own VM.
 */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        when (state.pane) {
            OnboardingPane.WELCOME -> WelcomePane(
                onStart = viewModel::startIntake,
                onSkip = viewModel::skip
            )
            OnboardingPane.FORM -> FormPane(
                form = state.form,
                submitting = state.submitting,
                error = state.submitError,
                onChange = viewModel::updateForm,
                onSubmit = viewModel::submit,
                onBack = viewModel::backToWelcome
            )
            OnboardingPane.STATUS -> StatusPane(
                launch = state.launch,
                statusError = state.statusError,
                onRefresh = viewModel::refreshStatus,
                onConnect = viewModel::goConnect
            )
            OnboardingPane.CONNECT -> ConnectPane(
                state = state,
                onCode = viewModel::setPairCode,
                onLabel = viewModel::setPairLabel,
                onPair = viewModel::pair,
                onBack = viewModel::backFromConnect
            )
            OnboardingPane.DONE -> DonePane(state = state)
        }
    }
}

@Composable
private fun WelcomePane(onStart: () -> Unit, onSkip: () -> Unit) {
    Text("Your own Caroline", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text(
        "Your own receptionist, on your own number, in your own system. " +
            "Fill in your business once — the system builds everything itself from there. " +
            "No payment is taken here; your number is issued new (your current number is not ported). " +
            "Voice is live at launch; texting turns on by itself when the carriers finish verifying your registration."
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start my Caroline") }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
        Text("I already have a Caroline — sign in instead")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormPane(
    form: IntakeFormState,
    submitting: Boolean,
    error: String?,
    onChange: (IntakeFormState) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Text("Start your Caroline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    SectionTitle("Your business")
    Field(form.businessName, "Business name (as customers know it)") { onChange(form.copy(businessName = it)) }
    Field(form.tradeBlurb, "What the business does (a sentence or two)", required = false) { onChange(form.copy(tradeBlurb = it)) }
    Field(form.greeting, "Greeting, if you want your own words (optional)", required = false) { onChange(form.copy(greeting = it)) }
    ChoiceField("Industry", form.industry, SubscriberIntake.INDUSTRIES) { onChange(form.copy(industry = it)) }

    SectionTitle("You, the owner")
    Field(form.ownerName, "Owner's full name") { onChange(form.copy(ownerName = it)) }
    Field(form.ownerCell, "Owner's cell phone") { onChange(form.copy(ownerCell = it)) }
    Field(form.email, "Owner's email") { onChange(form.copy(email = it)) }
    Field(form.timezone, "Timezone", required = false) { onChange(form.copy(timezone = it)) }

    SectionTitle("Your new number")
    Field(form.areaCode, "Area code for your new business number") { onChange(form.copy(areaCode = it)) }

    SectionTitle("Carrier registration (so your texting can be verified)")
    Field(form.legalName, "Legal business name", required = false) { onChange(form.copy(legalName = it)) }
    Field(form.ein, "EIN / tax ID") { onChange(form.copy(ein = it)) }
    ChoiceField("Business type", form.entityType, SubscriberIntake.ENTITY_TYPES) { onChange(form.copy(entityType = it)) }
    ChoiceField("Company type", form.companyType, SubscriberIntake.COMPANY_TYPES) { onChange(form.copy(companyType = it)) }
    Field(form.street, "Business street address") { onChange(form.copy(street = it)) }
    Field(form.city, "City") { onChange(form.copy(city = it)) }
    Field(form.state, "State (e.g. OH)") { onChange(form.copy(state = it)) }
    Field(form.zip, "ZIP") { onChange(form.copy(zip = it)) }
    Field(form.website, "Website (optional)", required = false) { onChange(form.copy(website = it)) }

    if (error != null) {
        Spacer(Modifier.height(8.dp))
        Text(error, color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(20.dp))
    Button(onClick = onSubmit, enabled = !submitting, modifier = Modifier.fillMaxWidth()) {
        if (submitting) CircularProgressIndicator(modifier = Modifier.height(18.dp))
        else Text("Launch my Caroline")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(18.dp))
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Field(value: String, label: String, required: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(if (required) label else label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onChange(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun StatusPane(launch: LaunchStatus?, statusError: String?, onRefresh: () -> Unit, onConnect: () -> Unit) {
    val context = LocalContext.current
    Text("Building your Caroline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    if (launch == null) {
        Text(statusError ?: "Asking the platform for your launch…")
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Check again") }
        return
    }
    Text("${launch.businessName} — launch status: ${launch.status}")
    Spacer(Modifier.height(12.dp))

    if (launch.isBlockedAtPreflight) {
        Text("⛔ The platform is being finished — your launch is held before any charge, and this page keeps checking.")
        val reasons = launch.steps.firstOrNull { it.key == "preflight" }?.reasons.orEmpty()
        reasons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(8.dp))
    }

    launch.steps.forEach { step ->
        val mark = when {
            step.isDone -> "✅"
            step.isBlocked -> "⛔"
            step.isFailed -> "⚠️"
            step.isWaiting -> "⏳"
            step.status.isNotEmpty() -> "…"
            else -> "•"
        }
        Text("$mark ${step.label}")
        step.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        if (step.key != "preflight") step.reasons.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
    }

    val conf = launch.confirmation
    if (launch.isDone && conf != null) {
        Spacer(Modifier.height(20.dp))
        Text("Your Caroline is live", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Your business number: ${conf.businessNumber}", fontWeight = FontWeight.Bold)
        Text("Call it and she answers, by your business's name.")
        conf.texting?.let { Spacer(Modifier.height(8.dp)); Text("Texting: $it") }
        conf.facilitation?.let { Spacer(Modifier.height(8.dp)); Text(it) }
        conf.admin?.let { Spacer(Modifier.height(8.dp)); Text("Admin: $it") }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${conf.businessNumber}")))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Call my number") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(conf.crmUrl))) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open my office (CRM & projects)") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onConnect, modifier = Modifier.fillMaxWidth()) { Text("Connect this phone to my Caroline") }
    } else {
        if (statusError != null) {
            Spacer(Modifier.height(8.dp))
            Text(statusError, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Refresh status") }
        Text(
            "You can close the app — your launch keeps building, and this screen picks it back up when you return.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ConnectPane(
    state: OnboardingUiState,
    onCode: (String) -> Unit,
    onLabel: (String) -> Unit,
    onPair: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val crmUrl = state.launch?.confirmation?.crmUrl
    Text("Connect this phone", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text(
        "Pairing ties this phone to YOUR Caroline, on your own system. Make a one-time pairing code in your Office, " +
            "then type it here. The code works once; this phone keeps its own key, and you can revoke it from your Office."
    )
    if (crmUrl != null) {
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(crmUrl))) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open my Office to make the code") }
    }
    Spacer(Modifier.height(16.dp))
    Field(state.pairCode, "One-time pairing code") { onCode(it) }
    Field(state.pairLabel, "This phone's name", required = false) { onLabel(it) }
    state.boundOfficeUrl?.let {
        Text("Your system: $it", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
    }
    state.pairNote?.let {
        Text(it, color = if (state.paired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
    }
    Button(onClick = onPair, enabled = !state.pairing, modifier = Modifier.fillMaxWidth()) {
        if (state.pairing) CircularProgressIndicator(modifier = Modifier.height(18.dp)) else Text("Pair this phone")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to launch status") }
}

@Composable
private fun DonePane(state: OnboardingUiState) {
    Text("You're live", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text("This phone is paired with your Caroline${state.launch?.businessName?.let { " at $it" } ?: ""}.")
    state.launch?.confirmation?.businessNumber?.let { Text("Your business number: $it", fontWeight = FontWeight.Bold) }
}
