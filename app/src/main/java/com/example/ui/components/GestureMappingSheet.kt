package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.GestureAction
import com.example.actions.GesturePresetProfile
import com.example.gesture.GestureCategory
import com.example.gesture.GestureType
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureMappingSheet(
  mappings: Map<GestureType, GestureAction>,
  onUpdateMapping: (GestureType, GestureAction) -> Unit,
  onApplyPreset: (GesturePresetProfile) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedCategory by remember { mutableStateOf<GestureCategory?>(null) }
  var editingGesture by remember { mutableStateOf<GestureType?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SleekCardBgDarker,
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(20.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Gesture Action Mapping",
            color = SleekTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Customize action triggered for each gesture",
            color = SleekTextMuted,
            fontSize = 12.sp
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = SleekTextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Preset Quick Selectors
      Text(
        text = "PRESET PROFILES",
        color = SleekSkyBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GesturePresetProfile.entries.forEach { profile ->
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(SleekCardBg)
              .border(1.dp, SleekBorder, RoundedCornerShape(10.dp))
              .clickable { onApplyPreset(profile) }
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = profile.title.split(" ").first(),
              color = SleekTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { selectedCategory = null },
          label = { Text("All (${GestureType.entries.count { it != GestureType.NONE }})") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SleekSkyBlue.copy(alpha = 0.2f),
            selectedLabelColor = SleekSkyBlue
          )
        )
        GestureCategory.entries.filter { it != GestureCategory.NONE }.forEach { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SleekSkyBlue.copy(alpha = 0.2f),
              selectedLabelColor = SleekSkyBlue
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Gestures List
      val displayedGestures = GestureType.entries.filter {
        it != GestureType.NONE && (selectedCategory == null || it.category == selectedCategory)
      }

      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(displayedGestures) { gesture ->
          val currentAction = mappings[gesture] ?: GestureAction.NONE

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(SleekCardBg)
              .border(1.dp, SleekBorder, RoundedCornerShape(14.dp))
              .clickable { editingGesture = gesture }
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = gesture.displayName,
                    color = SleekTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = gesture.category.label,
                    color = SleekTextDarkMuted,
                    fontSize = 10.sp
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = gesture.description,
                  color = SleekTextMuted,
                  fontSize = 11.sp
                )
              }

              // Mapped Action Tag
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (currentAction != GestureAction.NONE) SleekSkyBlue.copy(alpha = 0.15f) else SleekCardBgDarker)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = currentAction.displayName,
                  color = if (currentAction != GestureAction.NONE) SleekSkyBlue else SleekTextDarkMuted,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit",
                  tint = SleekSkyBlue,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            // Action Selection Menu
            if (editingGesture == gesture) {
              DropdownMenu(
                expanded = true,
                onDismissRequest = { editingGesture = null }
              ) {
                GestureAction.entries.forEach { action ->
                  DropdownMenuItem(
                    text = {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column {
                          Text(
                            text = action.displayName,
                            fontWeight = if (currentAction == action) FontWeight.Bold else FontWeight.Normal
                          )
                          Text(
                            text = action.description,
                            fontSize = 11.sp,
                            color = SleekTextMuted
                          )
                        }
                        if (currentAction == action) {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = SleekEmerald,
                            modifier = Modifier.size(16.dp)
                          )
                        }
                      }
                    },
                    onClick = {
                      onUpdateMapping(gesture, action)
                      editingGesture = null
                    }
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
