package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionSafetyLevel
import com.example.model.ProposedAction
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBgDark
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun ActionConfirmationDialog(
  action: ProposedAction,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  val safetyColor = when (action.safetyLevel) {
    ActionSafetyLevel.SENSITIVE_EXECUTE -> SleekRoseAlert
    ActionSafetyLevel.EXECUTE -> SleekAmber
    ActionSafetyLevel.SUGGEST -> SleekSkyBlue
    ActionSafetyLevel.READ -> SleekEmerald
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SleekCardBg,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (action.safetyLevel == ActionSafetyLevel.SENSITIVE_EXECUTE) Icons.Default.Warning else Icons.Default.Shield,
          contentDescription = null,
          tint = safetyColor,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Confirm Action",
          color = SleekTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column {
        Text(
          text = action.title,
          color = SleekSkyBlue,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = action.description,
          color = SleekTextSecondary,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = SleekCardBgDarker,
          border = BorderStroke(1.dp, safetyColor.copy(alpha = 0.5f))
        ) {
          Text(
            text = "SAFETY: ${action.safetyLevel.name}",
            color = safetyColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.testTag("confirm_action_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = safetyColor)
      ) {
        Text("Execute", color = SleekBgDark, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dismiss_action_btn"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SleekBorder)
      ) {
        Text("Cancel", color = SleekTextSecondary)
      }
    }
  )
}

