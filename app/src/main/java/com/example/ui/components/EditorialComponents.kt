package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBurgundy
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.UiMessage

@Composable
fun EditorialSectionHeader(
  kicker: String,
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
  actionButton: (@Composable () -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = kicker.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )
      if (!subtitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    if (actionButton != null) {
      Spacer(modifier = Modifier.width(12.dp))
      actionButton()
    }
  }
}

@Composable
fun EditorialCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  testTag: String = "editorial_card",
  content: @Composable () -> Unit
) {
  OutlinedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      content()
    }
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  testTag: String = "metric_card",
  onClick: (() -> Unit)? = null
) {
  OutlinedCard(
    modifier = modifier
      .testTag(testTag)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .background(accentColor.copy(alpha = 0.12f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon) = when (status.uppercase()) {
    "ACTIVE", "COMPLETED", "CONNECTED", "PUBLISHED", "APPROVED" ->
      Triple(AccentEmerald.copy(alpha = 0.15f), AccentEmerald, Icons.Default.CheckCircle)
    "EMERGING" ->
      Triple(ChampagneGold.copy(alpha = 0.18f), ChampagneGold, Icons.Default.Info)
    "PEAKING", "RUNNING", "GENERATING", "SYNCING" ->
      Triple(AccentCobalt.copy(alpha = 0.15f), AccentCobalt, Icons.Default.Schedule)
    "REVIEW", "QUEUED", "NICHE" ->
      Triple(AccentAmber.copy(alpha = 0.15f), AccentAmber, Icons.Default.Schedule)
    "FAILED", "ERROR", "CANCELLED", "DECLINING" ->
      Triple(AccentBurgundy.copy(alpha = 0.2f), AccentBurgundy, Icons.Default.Error)
    else -> // Draft, Not_Tested, Inactive, Idea, Outline
      Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, null)
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = bgColor
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = status.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = textColor,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
    }
  }
}

@Composable
fun EmptyStateView(
  title: String,
  description: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  actionTestTag: String = "empty_state_action",
  onActionClick: (() -> Unit)? = null
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    if (actionText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(20.dp))
      Button(
        onClick = onActionClick,
        modifier = Modifier.testTag(actionTestTag),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(actionText, style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
fun HauteButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  isPrimary: Boolean = true,
  isLoading: Boolean = false,
  enabled: Boolean = true,
  testTag: String = "haute_button"
) {
  if (isPrimary) {
    Button(
      onClick = onClick,
      modifier = modifier
        .heightIn(min = 48.dp)
        .testTag(testTag),
      enabled = enabled && !isLoading,
      shape = RoundedCornerShape(8.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
      )
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(18.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
      } else {
        if (icon != null) {
          Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      modifier = modifier
        .heightIn(min = 48.dp)
        .testTag(testTag),
      enabled = enabled && !isLoading,
      shape = RoundedCornerShape(8.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onBackground
      )
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(18.dp),
          color = MaterialTheme.colorScheme.primary,
          strokeWidth = 2.dp
        )
      } else {
        if (icon != null) {
          Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun SystemMessageBanner(
  message: UiMessage,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (bg, contentColor, icon) = when (message) {
    is UiMessage.Success -> Triple(AccentEmerald, Color.White, Icons.Default.CheckCircle)
    is UiMessage.Error -> Triple(AccentBurgundy, Color.White, Icons.Default.Error)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(8.dp),
    color = bg
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = when (message) {
            is UiMessage.Success -> message.message
            is UiMessage.Error -> message.message
          },
          style = MaterialTheme.typography.bodyMedium,
          color = contentColor,
          fontWeight = FontWeight.Medium
        )
      }
      IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss",
          tint = contentColor.copy(alpha = 0.8f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
