package ma.ensa.projet.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R

class SwipeGestureCallback(
    private val onDelete: ((Int) -> Unit)? = null,
    private val onStatusUpdate: ((Int) -> Unit)? = null,
    private val swipeDirections: Int = SWIPE_RIGHT // Default direction
) : ItemTouchHelper.SimpleCallback(0, swipeDirections) {

    companion object {
        const val SWIPE_RIGHT = ItemTouchHelper.RIGHT
        const val SWIPE_LEFT = ItemTouchHelper.LEFT
        const val SWIPE_BOTH = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.RIGHT -> onDelete?.invoke(position)
            ItemTouchHelper.LEFT -> onStatusUpdate?.invoke(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView

        when {
            dX > 0 && (swipeDirections and SWIPE_RIGHT) != 0 -> { // Swiping to the right (delete)
                val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_delete)
                val background = ColorDrawable(Color.RED)

                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + deleteIcon.intrinsicHeight
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + deleteIcon.intrinsicWidth

                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)

                if (dX > iconMargin) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }
            }
            dX < 0 && (swipeDirections and SWIPE_LEFT) != 0 -> { // Swiping to the left (update status)
                val updateIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_check)
                val background = ColorDrawable(Color.GREEN)

                val iconMargin = (itemView.height - updateIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + updateIcon.intrinsicHeight
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - updateIcon.intrinsicWidth

                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                if (-dX > iconMargin) {
                    updateIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    updateIcon.draw(c)
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}