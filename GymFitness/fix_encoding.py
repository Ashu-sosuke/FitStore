import codecs

path = 'd:/CodeHub/WOork/FitStore/GymFitness/app/src/main/java/com/example/gymfitness/presentation/components/PremiumComponents.kt'
with codecs.open(path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

# keep only the first 336 lines
lines = lines[:337]

content = "".join(lines)
content += """
@Composable
fun CodeChip(code: String, modifier: Modifier = Modifier) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.material3.Surface(
        color = com.example.gymfitness.ui.theme.OrangeTint,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        modifier = modifier
            .androidx.compose.foundation.border(1.dp, com.example.gymfitness.ui.theme.SunsetOrange.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .androidx.compose.foundation.clickable {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                android.widget.Toast.makeText(context, "Code copied!", android.widget.Toast.LENGTH_SHORT).show()
            }
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Code:",
                style = com.example.gymfitness.ui.theme.Typography.labelSmall,
                color = com.example.gymfitness.ui.theme.OrangeDeep
            )
            androidx.compose.material3.Text(
                text = code,
                style = com.example.gymfitness.ui.theme.Typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = com.example.gymfitness.ui.theme.SunsetOrange
            )
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Outlined.ContentCopy,
                contentDescription = "Copy Code",
                tint = com.example.gymfitness.ui.theme.SunsetOrange,
                modifier = androidx.compose.ui.Modifier.size(12.dp)
            )
        }
    }
}
"""

with codecs.open(path, 'w', encoding='utf-8') as f:
    f.write(content)
