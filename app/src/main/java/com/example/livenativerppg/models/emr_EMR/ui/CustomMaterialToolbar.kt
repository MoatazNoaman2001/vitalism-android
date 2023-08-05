package com.example.livenativerppg.models.emr_EMR.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import kotlin.math.roundToInt

class CustomMaterialToolbar(ctx: Context, attrs: AttributeSet) : MaterialToolbar(ctx, attrs){

    override fun setNavigationIcon(drawable: Drawable?) {
        super.setNavigationIcon(drawable)
        val iconSize = (ICON_SIZE_DP * (resources.displayMetrics.densityDpi / 160f)).roundToInt()
        val button = getNavigationIconButton(this)!!
        val iconLayoutParams = button.layoutParams
        iconLayoutParams.width = iconSize
        iconLayoutParams.height = iconSize

        button.scaleType = ImageView.ScaleType.FIT_CENTER

        button.layoutParams = iconLayoutParams
    }

   
    private fun getNavigationIconButton(toolbar: Toolbar): ImageButton? {
        val navigationIcon = toolbar.navigationIcon ?: return null
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ImageButton) {
                if (child.drawable === navigationIcon) {
                    return child
                }
            }
        }
        return null
    }

    companion object {
        const val ICON_SIZE_DP = 38
    }
}