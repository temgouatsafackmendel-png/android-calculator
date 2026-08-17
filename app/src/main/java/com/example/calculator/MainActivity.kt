package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

private val Background = Color(0xFF1C1C1E)
private val DisplayColor = Color(0xFFFFFFFF)
private val PreviewColor = Color(0xFF8E8E93)
private val DigitColor = Color(0xFF2C2C2E)
private val OperatorColor = Color(0xFFFF9F0A)
private val ActionColor = Color(0xFFA5A5A5)
private val ActionTextColor = Color(0xFF1C1C1E)

private val buttonRows = listOf(
    listOf("C", "⌫", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("±", "0", ".", "="),
)

private fun isOperator(ch: Char): Boolean = ch == '+' || ch == '-' || ch == '×' || ch == '÷'

@Composable
fun CalculatorApp() {
    var expression by remember { mutableStateOf("") }
    var justEvaluated by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    fun onPress(label: String) {
        when (label) {
            "C" -> {
                expression = ""
                justEvaluated = false
            }
            "⌫" -> {
                if (justEvaluated) {
                    expression = ""
                    justEvaluated = false
                } else {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                if (expression.isNotEmpty() && expression != "Erreur") {
                    val exprBefore = expression
                    val result = runCatching { evaluate(exprBefore) }.getOrNull()
                    if (result == null || result.isNaN() || result.isInfinite()) {
                        expression = "Erreur"
                    } else {
                        val formatted = formatNumber(result)
                        history = (listOf(exprBefore to formatted) + history).take(20)
                        expression = formatted
                    }
                    justEvaluated = true
                }
            }
            "+", "-", "×", "÷" -> {
                if (justEvaluated) {
                    justEvaluated = false
                    if (expression == "Erreur") expression = ""
                }
                if (expression.isEmpty() || expression == "Erreur") {
                    if (label == "-") expression = "-"
                } else if (expression.last().let(::isOperator)) {
                    expression = expression.dropLast(1) + label
                } else {
                    expression += label
                }
            }
            "%" -> {
                if (expression != "Erreur" && expression.isNotEmpty() &&
                    !expression.last().let(::isOperator)
                ) {
                    expression += "%"
                }
                justEvaluated = false
            }
            "±" -> {
                if (expression == "Erreur") {
                    expression = ""
                    justEvaluated = false
                } else {
                    expression = toggleSign(expression)
                    justEvaluated = false
                }
            }
            "." -> {
                if (justEvaluated) {
                    expression = "0."
                    justEvaluated = false
                } else if (expression.isEmpty() || expression == "Erreur") {
                    expression = "0."
                } else if (expression.last().let(::isOperator)) {
                    expression += "0."
                } else if (!currentNumber(expression).contains('.')) {
                    expression += "."
                }
            }
            else -> { // digits 0-9
                if (justEvaluated || expression == "Erreur") {
                    expression = ""
                    justEvaluated = false
                }
                expression += label
            }
        }
    }

    val preview = if (!justEvaluated && expression.any(::isOperator)) {
        runCatching { formatNumber(evaluate(expression)) }.getOrNull()
    } else {
        null
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            if (history.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top,
                ) {
                    history.take(6).forEach { (expr, res) ->
                        Text(
                            text = "$expr = $res",
                            color = PreviewColor,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                expression = res
                                justEvaluated = true
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (preview != null) {
                    Text(
                        text = preview,
                        color = PreviewColor,
                        fontSize = 28.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    color = DisplayColor,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in buttonRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (label in row) {
                            CalcButton(
                                label = label,
                                onClick = { onPress(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (background, foreground) = when (label) {
        "÷", "×", "-", "+", "=" -> OperatorColor to DisplayColor
        "C", "⌫", "%", "±" -> ActionColor to ActionTextColor
        else -> DigitColor to DisplayColor
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = foreground,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun currentNumber(expr: String): String =
    Regex("[0-9]*\\.?[0-9]*$").find(expr)?.value ?: ""

private fun toggleSign(expr: String): String {
    val match = Regex("[0-9]+(\\.[0-9]+)?$").find(expr) ?: return expr
    val start = match.range.first
    if (start > 0 && expr[start - 1] == '-' && (start - 1 == 0 || isOperator(expr[start - 2]))) {
        return expr.removeRange(start - 1, start)
    }
    return expr.substring(0, start) + "-" + expr.substring(start)
}

private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Erreur"
    if (value == 0.0) return "0"
    val longValue = value.toLong()
    if (value == longValue.toDouble() && kotlin.math.abs(value) < 1e15) {
        return longValue.toString()
    }
    val formatted = String.format(Locale.US, "%.10f", value)
        .trimEnd('0')
        .trimEnd('.')
    return if (formatted.isEmpty() || formatted == "-") "0" else formatted
}

private fun evaluate(expr: String): Double = Parser(expr).parse()

private class Parser(private val s: String) {
    private var i = 0

    fun parse(): Double {
        val result = parseExpression()
        if (i < s.length) throw IllegalArgumentException("unexpected character '${s[i]}'")
        return result
    }

    private fun parseExpression(): Double {
        var left = parseTerm()
        while (i < s.length) {
            when (s[i]) {
                '+' -> {
                    i++
                    left += parseTerm()
                }
                '-' -> {
                    i++
                    left -= parseTerm()
                }
                else -> return left
            }
        }
        return left
    }

    private fun parseTerm(): Double {
        var left = parseFactor()
        while (i < s.length) {
            when (s[i]) {
                '×' -> {
                    i++
                    left *= parseFactor()
                }
                '÷' -> {
                    i++
                    left /= parseFactor()
                }
                else -> return left
            }
        }
        return left
    }

    private fun parseFactor(): Double {
        if (i < s.length && s[i] == '-') {
            i++
            return -parseFactor()
        }
        if (i < s.length && s[i] == '+') {
            i++
            return parseFactor()
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Double {
        val start = i
        while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
        if (i == start) throw IllegalArgumentException("syntax error")
        var value = s.substring(start, i).toDouble()
        while (i < s.length && s[i] == '%') {
            i++
            value /= 100.0
        }
        return value
    }
}
