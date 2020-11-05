package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi.dummy

import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.ui.fixture.fixture_config.FixtureConfigFragment
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<DummyItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
       /* for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }*/
    }

    public fun addItem(item: DummyItem) {
        ITEMS.add(item)
        //ITEM_MAP.put("aaa", item)
    }

    public fun createDummyItem(code: String, status:Int): DummyItem {
        val dateTime:String = SimpleDateFormat("HH:mm:ss").format(java.util.Date())
        var statusMsg:String = ""
        when(status){
            FixtureController.NORMAL->{
                statusMsg = "OK"
            }
            FixtureController.ALREADY_ENTRY->{
                statusMsg = "検品済み"
            }
            FixtureController.INSERT_ERROR->{
                statusMsg = "追加に失敗しました"
            }
            FixtureController.UPDATE_ERROR->{
                statusMsg = "更新に失敗しました"
            }
        }
        return DummyItem(code, statusMsg, dateTime)
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class DummyItem(val content: String, val status: String, val dateTime:String) {
        override fun toString(): String = content + " ${dateTime}"
    }
}