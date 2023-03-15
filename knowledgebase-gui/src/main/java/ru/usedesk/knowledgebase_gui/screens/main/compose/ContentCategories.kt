package ru.usedesk.knowledgebase_gui.screens.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.usedesk_white_2))
    ) {
        ContentCategories(
            screen = State.Screen.Categories(
                State.Screen.Loading(),
                UsedeskSection(1L,
                    "",
                    null,
                    (1L..100L).map {
                        UsedeskCategory(it, "Title_$it", "Description_$it", listOf())
                    }
                )
            ),
            onEvent = {}
        )
    }
}

@Composable
internal fun ContentCategories(
    screen: State.Screen.Categories,
    onEvent: (Event) -> Unit
) {
    LazyColumnCard {
        items(
            items = screen.section.categories,
            key = UsedeskCategory::id
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(R.color.usedesk_white_1))
                    .clickableItem(
                        onClick = remember { { onEvent(Event.CategoryClicked(it)) } }
                    )
                    .padding(
                        start = 20.dp,
                        end = 10.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
            ) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    style = TextStyle(
                        fontSize = 17.sp,
                        textAlign = TextAlign.Start,
                        color = colorResource(R.color.usedesk_black_2)
                    ),
                    text = it.title
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 10.dp)
                            .weight(weight = 1f, fill = true),
                        style = TextStyle(
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            color = colorResource(R.color.usedesk_gray_cold_2)
                        ),
                        text = it.description
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(24.dp),
                        painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }
}