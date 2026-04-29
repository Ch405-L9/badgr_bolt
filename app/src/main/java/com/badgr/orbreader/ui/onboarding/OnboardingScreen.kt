package com.badgr.orbreader.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badgr.orbreader.R
import com.badgr.orbreader.ui.theme.ReaderColors

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ReaderColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.onboarding_welcome),
                color = ReaderColors.orpFocal,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.onboarding_desc),
                color = ReaderColors.textWarm,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = ReaderColors.orpFocal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.get_started),
                    color = ReaderColors.background,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
