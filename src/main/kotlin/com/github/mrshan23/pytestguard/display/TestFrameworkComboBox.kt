package com.github.mrshan23.pytestguard.display

import com.github.mrshan23.pytestguard.test.TestFramework
import com.intellij.openapi.ui.ComboBox
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class TestFrameworkComboBox : ComboBox<TestFramework>(TestFramework.entries.toTypedArray()) {
    init {
        renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean,
            ): Component {
                var name = value
                if (value is TestFramework) {
                    name = value.frameworkName
                }
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus)
            }
        }
    }
}