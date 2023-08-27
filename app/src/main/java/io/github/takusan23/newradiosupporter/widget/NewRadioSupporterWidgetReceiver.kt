package io.github.takusan23.newradiosupporter.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * ウィジェットの作成に必要な RemoteView を作ってくれる
 * レイアウトは [NewRadioSupporterWidget] でやります
 */
class NewRadioSupporterWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget
        get() = NewRadioSupporterWidget()

}