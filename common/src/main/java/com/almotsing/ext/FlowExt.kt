package com.almotsing.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
fun <E> ProducerScope<E>.offerIfNotClosed(element: E) {
    if (!isClosedForSend) {
        trySend(element)
    }
}

fun TextView.textChanges(
    skipInitialValue: Boolean = false,
    debounce: Long = 300L
): Flow<CharSequence?> =
    callbackFlow<CharSequence?> {
        val listener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                offerIfNotClosed(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        this@textChanges.addTextChangedListener(listener)
        awaitClose {
            removeTextChangedListener(listener)
        }
    }

@OptIn(FlowPreview::class)
fun EditText.textChanges(skipInitialValue: Boolean = false, debounce: Long = 300L): Flow<CharSequence?> =
    callbackFlow<CharSequence?> {
        val listener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                offerIfNotClosed(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        this@textChanges.addTextChangedListener(listener)
        awaitClose {
            removeTextChangedListener(listener)
        }
    }.buffer(Channel.CONFLATED)
        .drop(dropInitialValueIfSkipped(skipInitialValue))
        .debounce(debounce)

private fun dropInitialValueIfSkipped(skipInitialValue: Boolean) = if (skipInitialValue) 1 else 0