package com.example.introtocompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.introtocompose.ui.theme.IntroToComposeTheme
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntroToComposeTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val count = remember { mutableIntStateOf(1)}
    val totalPerPerson = remember { mutableDoubleStateOf(0.0)}
    val tipPercentage = remember { mutableIntStateOf(30)}
    val billAmount = remember { mutableStateOf("")}
    val tipAmount = remember { mutableDoubleStateOf(0.0)}
    val showOtherCardDetails = remember { mutableStateOf(false)}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TotalWidget(totalPerPerson = totalPerPerson.value)
            CardWidget {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextFieldWidget(
                        onValueChange = {
                            billAmount.value = it
                            if (it.isEmpty() || it == "0") {
                                tipAmount.value = 0.0
                                totalPerPerson.value = 0.0
                                showOtherCardDetails.value = false
                            } else {
                                tipAmount.doubleValue = (it.toDouble() * tipPercentage.intValue) / 100
                                billAmount.value = it
                                totalPerPerson.value = recalculateTotalPerPerson(
                                    billAmount = it.toDouble(),
                                    tipPercentage = tipPercentage.value,
                                    count = count.value
                                )
                                showOtherCardDetails.value = true
                                Log.d("MainActivity", "showOtherCardDetails: ${showOtherCardDetails.value}")
                            }
                        }
                    )
                    if (showOtherCardDetails.value) {
                        CardDetailsWidget(
                            addFunction = {
                                count.value++
                                totalPerPerson.value = recalculateTotalPerPerson(
                                    billAmount = billAmount.value.toDouble(),
                                    tipPercentage = tipPercentage.value,
                                    count = count.value
                                )
                            },
                            subtractFunction = {
                               if (count.value > 1) count.value--
                                totalPerPerson.value = recalculateTotalPerPerson(
                                    billAmount = billAmount.value.toDouble(),
                                    tipPercentage = tipPercentage.value,
                                    count = count.value
                                )
                            },
                            tipAmount = tipAmount.value,
                            count = count.value,
                            tipPercentage = tipPercentage.value,
                            onSliderChange = {
                                tipPercentage.value = (it * 100).toInt()
                                tipAmount.value = (billAmount.value.toDouble() * tipPercentage.value) / 100
                                totalPerPerson.value = recalculateTotalPerPerson(
                                    billAmount = billAmount.value.toDouble(),
                                    tipPercentage = tipPercentage.value,
                                    count = count.value
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardDetailsWidget(
    addFunction: () -> Unit = {},
    subtractFunction: () -> Unit = {},
    tipAmount: Double,
    count: Int,
    tipPercentage: Int,
    onSliderChange: (Float) -> Unit = {}
) {
    val rangeSliderValue = remember { mutableStateOf((tipPercentage).toFloat()/100) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Split")
            Row(
            ) {
                IconWidget(imageVector = Icons.Filled.Clear, contentDescription = "Subtract") {
                    subtractFunction()
                }
                Text(text = "$count", modifier = Modifier.padding(start = 8.dp, end = 8.dp), style = TextStyle(fontSize = 20.sp))
                IconWidget(imageVector = Icons.Filled.Add, contentDescription = "Add") {
                    addFunction()
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tip")
            Text(text = "$$tipAmount")
        }
        Text(text = "$tipPercentage%")
        Slider(steps = 51, value = rangeSliderValue.value, onValueChange = {
            rangeSliderValue.value = it
            onSliderChange(it)
        },
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
        )
    }
}

fun recalculateTotalPerPerson(
    billAmount: Double,
    tipPercentage: Int,
    count: Int
): Double {
    return roundOffDecimal((billAmount + (billAmount * tipPercentage) / 100) / count)
}

fun roundOffDecimal(number: Double): Double {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(number).toDouble()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconWidget(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Card (
        modifier = Modifier
            .height(50.dp)
            .width(50.dp)
            .clip(shape = RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun TotalWidget(
    totalPerPerson: Double
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .height(100.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Total Per Person",
            )
            Text(
                text = "$$totalPerPerson",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun CardWidget(content: @Composable () -> Unit) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(4.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWidget(
    onValueChange: (String) -> Unit = {}
) {
    val pattern = remember { Regex("^\\d+(\\.\\d*)?\$") }
    val value = remember { mutableStateOf("") }
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ){
        OutlinedTextField(
            value = value.value,
            onValueChange = {
                if (it.isEmpty() || it.matches(pattern)) {
                    value.value = it
                }
                onValueChange(value.value)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            leadingIcon =
            {
                Text(
                    text = "$",
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                )
            },
            label = {
                Text(
                    text = "Bill Amount",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
            )
        )
    }
}

@Composable
fun BoxWidget(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(100.dp)
            .width(100.dp)
            .clip(shape = RoundedCornerShape(50.dp))
            .background(Color.White)
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = "Tap me!",
            color = Color.Black
        )
    }
}
