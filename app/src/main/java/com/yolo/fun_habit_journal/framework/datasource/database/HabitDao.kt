package com.yolo.fun_habit_journal.framework.datasource.database

const val HABIT_ORDER_ASC: String = ""
const val HABIT_ORDER_DESC: String = "-"
const val HABIT_FILTER_TITLE = "title"
const val HABIT_FILTER_DATE_CREATED = "created_at"

const val ORDER_BY_ASC_DATE_UPDATED = HABIT_ORDER_ASC + HABIT_FILTER_DATE_CREATED
const val ORDER_BY_DESC_DATE_UPDATED = HABIT_ORDER_DESC + HABIT_FILTER_DATE_CREATED
const val ORDER_BY_ASC_TITLE = HABIT_ORDER_ASC + HABIT_FILTER_TITLE
const val ORDER_BY_DESC_TITLE = HABIT_ORDER_DESC + HABIT_FILTER_TITLE

const val HABIT_PAGINATION_PAGE_SIZE = 30

interface HabitDao {

}
