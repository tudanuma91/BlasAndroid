package com.v3.basis.blas.ui.terminal

import com.v3.basis.blas.R
import java.io.Serializable

enum class BottomNavButton(val id: Int, var title: Int) : Serializable {

    DASH_BOARD(R.id.navigation_dashboard, R.string.navi_title_terminal_dashboard),
    DATA_MANAGE(R.id.navigation_data_management, R.string.navi_title_terminal_item),
    EQUIPMENT_MANAGE(R.id.navigation_equipment_management, R.string.navi_title_terminal_fixture),
    LOGOUT(R.id.navigation_logout, R.string.navi_title_terminal_logout),
    STATUS(R.id.navigation_status, R.string.navi_title_terminal_status);

    companion object {
        fun find(position: Int): BottomNavButton {
            return values().get(position)
        }
        fun findById(id: Int): BottomNavButton? {
            return values().firstOrNull { id == it.id }
        }
    }
}
