package com.almotsing.ext


fun ByteArray.toHexString(hasSpace: Boolean = true) = this.joinToString(" ") {
    (it.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase() + if (hasSpace) " " else ""
}


fun ByteArray.readInt8(offset: Int = 0): Int {
    throwOffsetError(this, offset, 1)
    return this[offset].toInt()
}

fun ByteArray.readUInt8(offset: Int = 0): Int {
    throwOffsetError(this, offset, 1)
    return this[offset].toInt() and 0xFF
}

fun ByteArray.readInt16BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return (this[offset].toInt() shl 8) + (this[offset + 1].toInt() and 0xFF)
}

fun ByteArray.readUInt16BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
}

fun ByteArray.readInt16LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return (this[offset + 1].toInt() shl 8) + (this[offset].toInt() and 0xFF)
}

fun ByteArray.readUInt16LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset].toInt() and 0xFF)
}

fun ByteArray.readInt32BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 4)
    return (this[offset].toInt()) shl 24 or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)
}

fun ByteArray.readUInt32BE(offset: Int = 0): Long {
    throwOffsetError(this, offset, 4)
    return (((this[offset].toInt() and 0xFF).toLong() shl 24) or
            ((this[offset + 1].toInt() and 0xFF).toLong() shl 16) or
            ((this[offset + 2].toInt() and 0xFF).toLong() shl 8) or
            (this[offset + 3].toInt() and 0xFF).toLong())
}

fun ByteArray.readInt32LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 4)
    return (this[offset + 3].toInt()) shl 24 or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            (this[offset].toInt() and 0xFF)
}

fun ByteArray.readUInt32LE(offset: Int = 0): Long {
    throwOffsetError(this, offset, 4)
    return (((this[offset + 3].toInt() and 0xFF).toLong() shl 24) or
            ((this[offset + 2].toInt() and 0xFF).toLong() shl 16) or
            ((this[offset + 1].toInt() and 0xFF).toLong() shl 8) or
            (this[offset].toInt() and 0xFF).toLong())
}

fun ByteArray.readByteArrayBE(offset: Int, byteLength: Int): ByteArray {
    throwOffsetError(this, offset)
    return this.copyOfRange(
        offset,
        if ((offset + byteLength) > this.size) this.size else offset + byteLength
    )
}

fun ByteArray.writeInt8(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset)
    this[offset] = value.toByte()
    return this
}

fun ByteArray.writeInt16BE(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 2)
    this[offset] = (value shr 8 and 0xff).toByte()
    this[offset + 1] = (value and 0xff).toByte()
    return this
}

fun ByteArray.writeInt16LE(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 2)
    this[offset] = (value and 0xff).toByte()
    this[offset + 1] = (value and 0xff00 ushr 8).toByte()
    return this
}

fun ByteArray.writeInt32BE(value: Long, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    this[offset + 3] = (value and 0xff).toByte()
    this[offset + 2] = (value shr 8 and 0xff).toByte()
    this[offset + 1] = (value shr 16 and 0xff).toByte()
    this[offset] = (value shr 24 and 0xff).toByte()
    return this
}

fun ByteArray.writeInt32LE(value: Long, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    this[offset] = (value and 0xff).toByte()
    this[offset + 1] = (value and 0xff00 ushr 8).toByte()
    this[offset + 2] = (value and 0xff0000 ushr 16).toByte()
    this[offset + 3] = (value and 0xff000000 ushr 24).toByte()
    return this
}


fun ByteArray.insertByteArrayBE(
    insertArray: ByteArray,
    originalIndex: Int = 0,
    insertArrayOffset: Int = 0,
    insertArrayLength: Int = insertArray.size - insertArrayOffset
): ByteArray {
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex, this.size)
    val insertFinalArray =
        insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
    return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
}

fun ByteArray.insertByteArrayLE(
    insertArray: ByteArray,
    originalIndex: Int = 0,
    insertArrayOffset: Int = 0,
    insertArrayLength: Int = insertArray.size - insertArrayOffset
): ByteArray {
    insertArray.reverse()
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex, this.size)
    val insertFinalArray =
        insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
    return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
}

private fun throwLenError(byteArray: ByteArray, byteLength: Int) {
    if (byteLength <= 0 || byteLength > 4)
        throw IllegalArgumentException("The value of \"byteLength\" is out of range. It must be >= 1 and <= 4. Received $byteLength")
    if (byteLength > byteArray.size)
        throw IllegalArgumentException("Attempt to write outside ByteArray bounds.")
}

private fun throwHexError(hex: String) {
    if (hex.length % 2 != 0)
        throw IllegalArgumentException("The value of \"hex\".length is out of range. It must be an even number")
}

private fun throwOffsetError(
    byteArray: ByteArray,
    offset: Int,
    length: Int = 1,
    byteLength: Int = 0
) {
    if (offset > byteArray.size - length - byteLength)
        throw IllegalArgumentException("The value of \"offset\" is out of range. It must be >= 0 and <= ${byteArray.size - length - byteLength}. Received $offset")
}


