package paolo.udacity.core.utils

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout


@BindingAdapter("app:error")
fun errorOnTextInputLayout(view: TextInputLayout, error: String?) {
    view.error = error
}