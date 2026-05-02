package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.theme.SolariThemeVariant
import com.solari.app.ui.theme.ThemeMap
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ChangeThemeViewModel
import com.solari.app.ui.viewmodels.SettingsViewModel

@Composable
fun ChangeThemeScreen(
    viewModel: ChangeThemeViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(settingsViewModel.isDarkMode) {
        viewModel.loadSchemes(settingsViewModel.isDarkMode)
    }

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scaledClickable(pressedScale = 1.2f, onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SolariTheme.colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Change Theme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.onBackground
                )
            }
        },
        containerColor = SolariTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SolariTheme.colors.background)
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SolariTheme.colors.primary)
                }
            } else {
                Text(
                    text = "AVAILABLE THEMES",
                    fontSize = 12.sp * 1.4f,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.secondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.availableVariants) { variant ->
                        ThemeItem(
                            variant = variant,
                            isSelected = settingsViewModel.activeThemeVariant == variant,
                            onClick = {
                                settingsViewModel.setTheme(variant)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeItem(variant: SolariThemeVariant, isSelected: Boolean, onClick: () -> Unit) {
    val themeColors = ThemeMap[variant] ?: return
    
    Surface(
        color = if (isSelected) SolariTheme.colors.primary.copy(alpha = 0.2f) else SolariTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (isSelected) Modifier.border(1.dp, SolariTheme.colors.primary, RoundedCornerShape(12.dp)) else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = variant.displayName, 
                    color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isSelected) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SolariTheme.colors.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val previewColors = listOf(themeColors.primary, themeColors.secondary, themeColors.tertiary, themeColors.background, themeColors.surface)
                previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(color, RoundedCornerShape(2.dp))
                                .border(0.5.dp, SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        )
                }
            }
        }
    }
}
