package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DecisionType
import com.example.model.EventDecision
import com.example.model.ProposedAction
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBgDark
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekSkyBlueDark
import com.example.ui.theme.SleekSkyBlueLight
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DecisionCard(
  decision: EventDecision,
  onActionClick: ((ProposedAction) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val formattedTime = timeFormat.format(Date(decision.timestamp))

  val decisionColor = when (decision.decisionType) {
    DecisionType.SPEAK -> SleekSkyBlue
    DecisionType.SUGGEST -> SleekAmber
    DecisionType.ASK_CONFIRMATION -> SleekRoseAlert
    DecisionType.ACT -> SleekEmerald
    DecisionType.NOTIFY -> SleekAmber
    DecisionType.SILENT -> SleekTextDarkMuted
  }

  val decisionIcon = when (decision.decisionType) {
    DecisionType.SPEAK -> Icons.Default.RecordVoiceOver
    DecisionType.SUGGEST -> Icons.Default.Bolt
    DecisionType.ASK_CONFIRMATION -> Icons.Default.Warning
    DecisionType.ACT -> Icons.Default.CheckCircle
    DecisionType.NOTIFY -> Icons.Default.Notifications
    DecisionType.SILENT -> Icons.AutoMirrored.Filled.VolumeOff
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("decision_card_${decision.id}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = SleekCardBg.copy(alpha = 0.5f)),
    border = BorderStroke(1.dp, SleekBorder)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SleekCardBgDarker,
            border = BorderStroke(1.dp, SleekBorder)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = decisionIcon,
                contentDescription = null,
                tint = decisionColor,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = decision.decisionType.name,
                color = decisionColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = decision.eventType.name,
            color = SleekTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Text(
          text = formattedTime,
          color = SleekTextDarkMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Proactive Speech / Reason
      if (!decision.speechText.isNullOrBlank()) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SleekSkyBlue.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, SleekSkyBlue.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.RecordVoiceOver,
              contentDescription = "Spoken Speech",
              tint = SleekSkyBlue,
              modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "\"${decision.speechText}\"",
              color = SleekTextPrimary,
              fontSize = 13.sp,
              lineHeight = 18.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Reason text
      Text(
        text = decision.reason,
        color = SleekTextSecondary,
        fontSize = 12.sp,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Metrics Bars: Relevance & Interruptibility
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SleekCardBgDarker.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Relevance", color = SleekTextDarkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(
                "${(decision.relevanceScore * 100).toInt()}%",
                color = SleekSkyBlue,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { decision.relevanceScore.coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(4.dp),
              color = SleekSkyBlue,
              trackColor = SleekCardBgDarker
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Interruptibility", color = SleekTextDarkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(
                "${(decision.interruptScore * 100).toInt()}%",
                color = if (decision.interruptScore >= 0.5f) SleekRoseAlert else SleekEmerald,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { decision.interruptScore.coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(4.dp),
              color = if (decision.interruptScore >= 0.5f) SleekRoseAlert else SleekEmerald,
              trackColor = SleekCardBgDarker
            )
          }
        }
      }

      // Proposed Action Button if available
      if (decision.proposedAction != null) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
          onClick = { onActionClick?.invoke(decision.proposedAction) },
          modifier = Modifier.fillMaxWidth().testTag("action_button_${decision.proposedAction.id}"),
          border = BorderStroke(1.dp, SleekSkyBlue.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = SleekSkyBlue,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Suggested Action: ${decision.proposedAction.title}",
            color = SleekSkyBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
