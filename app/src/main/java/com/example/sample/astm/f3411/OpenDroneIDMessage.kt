package com.example.sample.astm.f3411

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.pow

fun Byte.bit(bit: Int): Int {
    if (bit !in 0..7) throw Exception()
    return ((this.toInt() and (2.0.pow(bit).toInt())).shr(bit))
}

fun Byte.bit(bit: Int, byte: Int): Byte {
    if (bit !in 0..7 || byte !in 0..1) throw Exception()
    return ((this.toInt() and ((2.0.pow(bit)).toInt().inv() and 0x000000FF)) or
            byte.shl(bit)).toByte()
}

fun Byte.top4bit() = ((this.toInt() and 0xF0) ushr (4)).toByte()
fun Byte.top4bit(byte: Int) = ((this.toInt() and 0x0F) or (byte.shl(4))).toByte()
fun Byte.lower4bit() = (this.toInt() and 0x0F).toByte()
fun Byte.lower4bit(byte: Int) = ((this.toInt() and 0xF0) or (byte and 0x0F)).toByte()
fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
fun String.toHex(): ByteArray {
    if (0 < (length % 2)) throw StringIndexOutOfBoundsException("String length odd")
    return ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

@ExperimentalUnsignedTypes
class OpenDroneIDMessage {
    companion object {
        fun parse(data: ByteArray) = Message(data)
    }

    class OpenDroneIDException(s: String) : Exception(s)

    enum class MessageType(val id: Int) {
        BasicID(0x00),
        LocationVector(0x01),
        Authentication(0x02),
        SelfID(0x03),
        System(0x04),
        OperatorID(0x05),
        MessagePack(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id }
                ?: throw OpenDroneIDException("Unsupported message type")

            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class IDType(val id: Int) {
        None(0x00),
        SerialNumber(0x01),
        RegistrationID(0x02),
        UUID(0x03),
        Invalid(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Invalid
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class UAType(val id: Int) {
        None(0x00),
        Aeroplane(0x01),
        Helicopter(0x02),
        Gyroplane(0x03),
        HybridLift(0x04),
        Ornithopter(0x05),
        Glider(0x06),
        Kite(0x07),
        FreeBalloon(0x08),
        CaptiveBalloon(0x09),
        Airship(0x0A),
        FreeFall(0x0B),
        Rocket(0x0C),
        TetheredPoweredAircraft(0x0D),
        GroundObstacle(0x0E),
        Other(0x0F);

        companion object {
            fun toID(id: Int): UAType = values().find { it.id == id } ?: Other
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class OperationalStatus(val id: Int) {
        Undeclared(0x00),
        Ground(0x01),
        Airborne(0x02),
        Reserved(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Reserved
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class HeightType(val id: Int) {
        AboveTakeoff(0x00),
        AGL(0x01);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: AboveTakeoff
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class DirectionSegment(val id: Int) {
        East(0),
        West(1);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: East
            fun toID(id: Byte) = toID(id.toInt())
        }

        fun toValue(): Int = if (this == East) 0 else 180
    }

    enum class SpeedMultiplier(val id: Int) {
        X025(0),
        X075(1);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: X025
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class HorizontalAccuracy(val id: Int) {
        Unknown(0x00),
        Acc18520m(0x01),
        Acc7408m(0x02),
        Acc3704m(0x03),
        Acc1852m(0x04),
        Acc926m(0x05),
        Acc555dp6m(0x06),
        Acc185dp2m(0x07),
        Acc92dp6m(0x08),
        Acc30m(0x09),
        Acc10m(0x0A),
        Acc3m(0x0B),
        Acc1m(0x0C),
        Reserved(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Reserved
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class VerticalAccuracy(val id: Int) {
        Unknown(0x00),
        Acc150m(0x01),
        Acc45m(0x02),
        Acc25m(0x03),
        Acc10m(0x04),
        Acc3m(0x05),
        Acc1m(0x06),
        Reserved(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Reserved
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class SpeedAccuracy(val id: Int) {
        Unknown(0x00),
        Acc10mps(0x01),
        Acc3mps(0x02),
        Acc1mps(0x03),
        Acc03mps(0x04),
        Reserved(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Reserved
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class OperatorLocationType(val id: Int) {
        TakeOff(0x00),
        LiveGNSS(0x01),
        FixedLocation(0x02),
        Reserved(0x03);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: Reserved
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    enum class AuthType(val id: Int) {
        None(0x00),
        UASIDSignature(0x01),
        OperatorIDSignature(0x02),
        MessageSetSignature(0x03),
        AuthenticationProvidedbyNetworkRemoteID(0x04),
        Reserved1(0x05),
        Reserved2(0x06),
        Reserved3(0x07),
        Reserved4(0x08),
        Reserved5(0x09),
        PrivateUse1(0x0A),
        PrivateUse2(0x0B),
        PrivateUse3(0x0C),
        PrivateUse4(0x0D),
        PrivateUse5(0x0E),
        PrivateUse6(0x0F);

        companion object {
            fun toID(id: Int) = values().find { it.id == id } ?: None
            fun toID(id: Byte) = toID(id.toInt())
        }
    }

    abstract class UasID(var data: ByteArray = ByteArray(SIZE)) {
        companion object {
            const val SIZE = 20
        }

        abstract val idType: IDType
        abstract override fun toString(): String
    }

    class NoneID(data: ByteArray = ByteArray(SIZE)) : UasID(data) {
        override val idType: IDType = IDType.None
        override fun toString(): String = ""

        constructor(str: String) : this(str.toByteArray())
    }

    class SerialNumber(data: ByteArray = ByteArray(SIZE)) : UasID(data) {
        override val idType: IDType = IDType.SerialNumber
        override fun toString(): String = String(data).trim(0x00.toChar())

        constructor(str: String) : this(str.toByteArray())
    }

    class RegistrationID(data: ByteArray = ByteArray(SIZE)) : UasID(data) {
        override val idType: IDType = IDType.RegistrationID
        override fun toString(): String = String(data).trim(0x00.toChar())

        constructor(str: String) : this(str.toByteArray())
    }

    class UUID(data: ByteArray = ByteArray(SIZE)) : UasID(data) {
        companion object {
            const val SIZE = 16
        }

        override val idType: IDType = IDType.UUID
        override fun toString(): String = data.sliceArray(0 until SIZE).toHexString()

        constructor(str: String) : this(str.toHex())
    }

    class InvalidID(data: ByteArray = ByteArray(SIZE)) : UasID(data) {
        override val idType: IDType = IDType.Invalid
        override fun toString(): String = ""
    }

    @ExperimentalUnsignedTypes
    class Message(payload: MessageData? = null) {
        companion object {
            const val SIZE = 25
        }

        constructor(data: ByteArray) : this() {
            header = Header(data[0])
            val messageData = data.sliceArray(1 until data.size)
            payload = when (header.type) {
                MessageType.BasicID -> BasicID(messageData)
                MessageType.LocationVector -> LocationVector(messageData)
                MessageType.Authentication -> when (messageData[0].lower4bit().toInt()) {
                    0 -> AuthenticationHeader(messageData)
                    else -> AuthenticationAdditional(messageData)
                }
                MessageType.SelfID -> SelfID(messageData)
                MessageType.System -> System(messageData)
                MessageType.OperatorID -> OperatorID(messageData)
                MessageType.MessagePack -> MessagePack(messageData)
            }
            header = Header(data[0])
        }

        var header: Header
        var payload: MessageData? = null
            set(value) {
                field = value
                if (null != value) {
                    header.type = when (value) {
                        is BasicID -> MessageType.BasicID
                        is LocationVector -> MessageType.LocationVector
                        is Authentication -> MessageType.Authentication
                        is SelfID -> MessageType.SelfID
                        is System -> MessageType.System
                        is OperatorID -> MessageType.OperatorID
                        is MessagePack -> MessageType.MessagePack
                        else -> MessageType.BasicID
                    }
                    header.version = value.version
                }
            }

        init {
            header = Header()
            this.payload = payload
        }

        fun toByteArray(): ByteArray {
            return byteArrayOf(header.data) + (payload?.data ?: ByteArray(MessageData.SIZE))
        }
    }

    class Header(var data: Byte = 0x00) {
        var type: MessageType
            get() = MessageType.toID(data.top4bit())
            set(value) {
                data = data.top4bit(value.id)
            }
        var version: Int
            get() = data.lower4bit().toInt()
            set(value) {
                data = data.lower4bit(value)
            }
    }

    open class MessageData(var data: ByteArray = ByteArray(SIZE)) {
        companion object {
            const val SIZE = 24
        }

        open var maxSize = SIZE
            protected set

        open var dataRange = SIZE..SIZE
            protected set

        open var version = 0
            protected set

        fun getLatLon(offset: Int): Double {
            ByteBuffer.wrap(data, offset, 4).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                return it.int / 10.0.pow(7)
            }
        }

        fun setLatLon(offset: Int, value: Double) {
            ByteBuffer.wrap(data, offset, 4).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                it.putInt((value * 10.0.pow(7)).toInt())
            }
        }

        @ExperimentalUnsignedTypes
        fun getAltitude(offset: Int): Double {
            ByteBuffer.wrap(data, offset, 2).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                return it.short.toUShort().toDouble() / 2 - 1000.0
            }
        }

        fun setAltitude(offset: Int, value: Double) {
            ByteBuffer.wrap(data, offset, 2).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                it.putShort(((value + 1000) * 2).toInt().toShort())
            }
        }
    }

    class BasicID(data: ByteArray = Default) : MessageData(data) {
        companion object {
            val Default = byteArrayOf(
                0x00,                                                        // IDType/UAType
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // UAS ID
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00                                             // Reserved
            )
        }

        var idType: IDType
            get() = IDType.toID(data[0].top4bit())
            private set(value) {
                data[0] = data[0].top4bit(value.id)
            }
        var uaType: UAType
            get() = UAType.toID(data[0].lower4bit())
            set(value) {
                data[0] = data[0].lower4bit(value.id)
            }
        var uasID: UasID
            get() = when (idType) {
                IDType.None -> NoneID(data.sliceArray(1..UasID.SIZE))
                IDType.SerialNumber -> SerialNumber(data.sliceArray(1..UasID.SIZE))
                IDType.RegistrationID -> RegistrationID(data.sliceArray(1..UasID.SIZE))
                IDType.UUID -> UUID(data.sliceArray(1..UasID.SIZE))
                IDType.Invalid -> InvalidID(data.sliceArray(1..UasID.SIZE))
            }
            set(value) {
                if (UasID.SIZE < value.data.size)
                    throw OpenDroneIDException("UAS ID length error")
                data = byteArrayOf(data[0]) + value.data +
                        ByteArray(maxSize - value.data.size - 1)
                idType = value.idType
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }
    }

    @ExperimentalUnsignedTypes
    class LocationVector(data: ByteArray = Default) : MessageData(data) {
        companion object {
            val Default = byteArrayOf(
                0x03,                   // Status/Flags/HeightType/E/W DirectionSegment/SpeedMultiplier
                0xB5.toByte(),          // TrackDirection
                0xFF.toByte(),          // Speed
                0x7E.toByte(),          // VerticalSpeed
                0x00, 0x00, 0x00, 0x00, // Latitude
                0x00, 0x00, 0x00, 0x00, // Longitude
                0x00, 0x00,             // PressureAltitude
                0x00, 0x00,             // GeodeticAltitude
                0x00, 0x00,             // Height
                0x00,                   // Horizontal/Vertical Accuracy
                0x00,                   // BaroAltitudeAccuracy/SpeedAccuracy
                0x00, 0x00,             // Timestamp
                0x01,                   // Reserved/TimestampAccuracy
                0x00                    // Reserved
            )
            val TrackDirectionRange = 0..359
            val VerticalSpeedRange = -62.0..62.0
            val LatitudeRange = -90.0..90.0
            val LongitudeRange = -180.0..180.0
            val AltitudeRange = -999.5..31767.0
            val TimestampRange = 0..36000
            val TimestampAccuracyRange = 0.1..1.5
            const val TrackDirectionUnknown = 361
            const val SpeedUnknown = 255.0
            const val VerticalSpeedUnknown = 63.0
            const val LatitudeUnknown = 0.0
            const val LongitudeUnknown = 0.0
            const val AltitudeUnknown = -1000.0
            const val TimestampAccuracyUnknown = 0.0
        }

        var status: OperationalStatus
            get() = OperationalStatus.toID(data[0].top4bit())
            set(value) {
                data[0] = data[0].top4bit(value.id)
            }

        var heightType: HeightType
            get() = HeightType.toID(data[0].bit(2))
            set(value) {
                data[0] = data[0].bit(2, value.id)
            }

        var directionSegment: DirectionSegment
            get() = DirectionSegment.toID(data[0].bit(1))
            set(value) {
                data[0] = data[0].bit(1, value.id)
            }

        var speedMultiplier: SpeedMultiplier
            get() = SpeedMultiplier.toID(data[0].bit(0))
            set(value) {
                data[0] = data[0].bit(0, value.id)
            }

        var trackDirection: Int
            get() {
                val td = data[1].toUByte().toInt()
                return if (td <= 180) td + directionSegment.toValue() else TrackDirectionUnknown
            }
            set(value) {
                if (value in TrackDirectionRange || value == TrackDirectionUnknown)
                    if (value < 180) {
                        data[1] = value.toByte()
                        directionSegment = DirectionSegment.East
                    } else {
                        data[1] = (value - 180).toByte()
                        directionSegment = DirectionSegment.West
                    }
                else
                    throw OpenDroneIDException("Directional range error")
            }

        var speed: Double
            get() {
                val s = data[2].toUByte().toInt()
                return if (SpeedMultiplier.X025 == speedMultiplier)
                    s * 0.25 else (s * 0.75) + (255 * 0.25)
            }
            set(value) {
                if (value <= 225 * 0.25) {
                    data[2] = (value / 0.25).toInt().toByte()
                    speedMultiplier = SpeedMultiplier.X025
                } else if (225 * 0.25 < value && value < 254.25) {
                    data[2] = ((value - (225 * 0.25)) / 0.75).toInt().toByte()
                    speedMultiplier = SpeedMultiplier.X075
                } else if (value == SpeedUnknown) {
                    data[2] = 255.toByte()
                    speedMultiplier = SpeedMultiplier.X075
                } else {
                    data[2] = 254.toByte()
                    speedMultiplier = SpeedMultiplier.X075
                }
            }

        var verticalSpeed: Double
            get() = if ((VerticalSpeedRange.endInclusive / 0.5) < abs(data[3].toInt()))
                VerticalSpeedUnknown else data[3] * 0.5
            set(value) {
                data[3] = when {
                    value == VerticalSpeedUnknown -> (VerticalSpeedUnknown / 0.5)
                    value < VerticalSpeedRange.start -> (VerticalSpeedRange.start / 0.5)
                    value > VerticalSpeedRange.endInclusive -> (VerticalSpeedRange.endInclusive / 0.5)
                    else -> (value / 0.5)
                }.toInt().toByte()
            }

        var latitude: Double
            get() {
                val lat = getLatLon(4)
                return if (lat in LatitudeRange) lat else LatitudeUnknown
            }
            set(value) {
                if (value in LatitudeRange || value == LatitudeUnknown)
                    setLatLon(4, value)
                else
                    throw OpenDroneIDException("Latitude range error")
            }

        var longitude: Double
            get() {
                val lon = getLatLon(8)
                return if (lon in LongitudeRange) lon else LongitudeUnknown
            }
            set(value) {
                if (value in LongitudeRange || value == LongitudeUnknown)
                    setLatLon(8, value)
                else
                    throw OpenDroneIDException("Longitude range error")
            }

        var pressureAltitude: Double
            get() {
                val alt = getAltitude(12)
                return if (alt in AltitudeRange) alt else AltitudeUnknown
            }
            set(value) {
                if (value in AltitudeRange || value == AltitudeUnknown)
                    setAltitude(12, value)
                else
                    throw OpenDroneIDException("Altitude range error")
            }

        var geodeticAltitude: Double
            get() {
                val alt = getAltitude(14)
                return if (alt in AltitudeRange) alt else AltitudeUnknown
            }
            set(value) {
                if (value in AltitudeRange || value == AltitudeUnknown)
                    setAltitude(14, value)
                else
                    throw OpenDroneIDException("Altitude range error")
            }

        var height: Double
            get() {
                val alt = getAltitude(16)
                return if (alt in AltitudeRange) alt else AltitudeUnknown
            }
            set(value) {
                if (value in AltitudeRange || value == AltitudeUnknown)
                    setAltitude(16, value)
                else
                    throw OpenDroneIDException("Altitude range error")
            }

        var horizontalAccuracy: HorizontalAccuracy
            get() = HorizontalAccuracy.toID(data[18].top4bit())
            set(value) {
                data[18] = data[18].top4bit(value.id)
            }

        var verticalAccuracy: VerticalAccuracy
            get() = VerticalAccuracy.toID(data[18].lower4bit())
            set(value) {
                data[18] = data[18].lower4bit(value.id)
            }

        var baroAltitudeAccuracy: VerticalAccuracy
            get() = VerticalAccuracy.toID(data[19].top4bit())
            set(value) {
                data[19] = data[19].top4bit(value.id)
            }

        var speedAccuracy: SpeedAccuracy
            get() = SpeedAccuracy.toID(data[19].lower4bit())
            set(value) {
                data[19] = data[19].lower4bit(value.id)
            }

        var timestamp100msec: Int
            get() {
                val ts = ByteBuffer.wrap(data, 20, 2).let {
                    it.order(ByteOrder.LITTLE_ENDIAN)
                    (it.short.toUShort()).toInt()
                }
                return if (ts in TimestampRange) ts else 0
            }
            set(value) {
                if (value in TimestampRange)
                    ByteBuffer.wrap(data, 20, 2).let {
                        it.order(ByteOrder.LITTLE_ENDIAN)
                        it.putShort(value.toShort())
                    }
                else
                    throw OpenDroneIDException("Timestamp range error")
            }

        var timestampAccuracy: Double
            get() = data[22].lower4bit().toDouble() * 0.1
            set(value) {
                if (value in TimestampAccuracyRange || value == TimestampAccuracyUnknown)
                    data[22] = data[22].lower4bit((value / 0.1).toInt())
                else
                    throw OpenDroneIDException("Timestamp Accuracy range error")
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }

        fun isUnknown(): Boolean =
            latitude == LatitudeUnknown && longitude == LongitudeUnknown
    }


    abstract class Authentication(data: ByteArray) : MessageData(data) {
        companion object {
            val PageNumberRange = 0..5
        }
        var authType: AuthType
            get() = AuthType.toID(data[0].top4bit())
            set(value) {
                data[0] = data[0].top4bit(value.id)
            }

        var pageNumber: Int
            get() = data[0].lower4bit().toInt()
            set(value) {
                if (value !in pageNumberRange())
                    throw OpenDroneIDException("Page Number range error")
                data[0] = data[0].lower4bit(value)
            }

        abstract var authData: ByteArray
        abstract fun pageNumberRange(): IntRange
        abstract fun authDataLength(): Int
    }

    class AuthenticationHeader(data: ByteArray = Default) : Authentication(data) {
        companion object {
            val Default = byteArrayOf(
                0x00,                                                       // AuthType/PageNumber
                0x01,                                                       // PageCount
                0x11,                                                       // Length
                0x00, 0x00, 0x00, 0x00,                                     // Timestamp
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // AuthData
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
            val AuthDataLength = 17
            val PageNumberRange = 0..0
            val PageCountRange = 0..5
            val lengthRange = 0..109
            val UnixTimeRange = 1546300800..3693752040
        }

        var pageCount: Int
            get() = data[1].lower4bit().toInt()
            set(value) {
                if (value !in PageCountRange)
                    throw OpenDroneIDException("Page Count range error")
                data[1] = data[1].lower4bit(value)
            }

        var length: Int
            get() = data[2].toUByte().toInt()
            set(value) {
                if (value !in lengthRange)
                    throw OpenDroneIDException("Length range error")
                data[2] = value.toByte()
            }

        var unixTime: Long
            get() = ByteBuffer.wrap(data, 3, 4).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                return (it.int.toUInt().toLong() + 1546300800)
            }
            set(value) {
                if (value !in UnixTimeRange)
                    throw OpenDroneIDException("UnixTime range error")
                ByteBuffer.wrap(data, 3, 4).let {
                    it.order(ByteOrder.LITTLE_ENDIAN)
                    it.putInt((value - 1546300800).toInt())
                }
            }

        override var authData: ByteArray
            get() = data.sliceArray(7 until SIZE)
            set(value) {
                if (value.size != AuthDataLength)
                    throw OpenDroneIDException("Auth Data length error")
                data = data.sliceArray(0..6) + value
            }

        override fun pageNumberRange() = PageNumberRange
        override fun authDataLength() = AuthDataLength

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }
    }

    class AuthenticationAdditional(data: ByteArray = Default) : Authentication(data) {
        companion object {
            val Default = byteArrayOf(
                0x01,                                                       // AuthType/PageNumber
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // AuthData
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00
            )
            val AuthDataLength = 23
            val PageNumberRange = 1..5
        }

        override var authData: ByteArray
            get() = data.sliceArray(1 until SIZE)
            set(value) {
                if (value.size != AuthDataLength)
                    throw OpenDroneIDException("Auth Data length error")
                data = byteArrayOf(data[0]) + value
            }

        override fun pageNumberRange() = PageNumberRange
        override fun authDataLength() = AuthDataLength
    }

    @ExperimentalUnsignedTypes
    class SelfID(data: ByteArray = ByteArray(SIZE)) : MessageData(data) {
        val descriptionTypeRange = 0..255
        var descriptionType: Int
            get() = data[0].toUByte().toInt()
            set(value) {
                if (value !in descriptionTypeRange)
                    throw OpenDroneIDException("Description Type range error")
                data[0] = value.toByte()
            }

        val descriptionMaxLength = 23
        var description: String
            get() = String(data.sliceArray(1..descriptionMaxLength)).trim(0x00.toChar())
            set(value) {
                if (descriptionMaxLength < value.length)
                    throw OpenDroneIDException("Description length error")
                data = byteArrayOf(data[0]) + value.toByteArray() +
                        ByteArray(maxSize - value.length - 1)
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }
    }

    @ExperimentalUnsignedTypes
    class System(data: ByteArray = ByteArray(SIZE)) : MessageData(data) {
        var operatorLocationtype: OperatorLocationType
            get() = OperatorLocationType.toID(data[0].toInt() and 0x03)
            set(value) {
                data[0] = value.id.toByte()
            }

        val operatorLatitudeRange = -90.0..90.0
        var operatorLatitude: Double
            get() = getLatLon(1)
            set(value) {
                if (value !in operatorLatitudeRange)
                    throw OpenDroneIDException("Latitude range error")
                setLatLon(1, value)
            }

        val operatorLogitudeRange = -180.0..180.0
        var operatorLogitude: Double
            get() = getLatLon(5)
            set(value) {
                if (value !in operatorLogitudeRange)
                    throw OpenDroneIDException("Latitude range error")
                setLatLon(5, value)
            }

        val areaCountRange = 1..65000
        var areaCount: Int
            get() = ByteBuffer.wrap(data, 9, 2).let {
                it.order(ByteOrder.LITTLE_ENDIAN)
                return it.short.toUShort().toInt()
            }
            set(value) {
                if (value !in areaCountRange)
                    throw OpenDroneIDException("Area Count range error")
                ByteBuffer.wrap(data, 9, 2).let {
                    it.order(ByteOrder.LITTLE_ENDIAN)
                    it.putShort(value.toShort())
                }
            }

        val areaRadiusRange = 0.0..2.5
        var areaRadius: Double
            get() = data[11].toDouble() / 10
            set(value) {
                if (value !in areaRadiusRange)
                    throw OpenDroneIDException("Area Radius range error")
                data[11] = (value * 10).toInt().toByte()
            }

        val altitudeRange = -1000.0..31767.0
        var areaCeiling: Double
            get() = getAltitude(12)
            set(value) {
                if (value !in altitudeRange)
                    throw OpenDroneIDException("Altitude range error")
                setAltitude(12, value)
            }

        var areaFloor: Double
            get() = getAltitude(14)
            set(value) {
                if (value !in altitudeRange)
                    throw OpenDroneIDException("Altitude range error")
                setAltitude(14, value)
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
            if (0 == areaCount) areaCount = 1
        }
    }

    @ExperimentalUnsignedTypes
    class OperatorID(data: ByteArray = ByteArray(SIZE)) : MessageData(data) {
        val operatorIDTypeRange = 0..255
        var operatorIDType: Int
            get() = data[0].toUByte().toInt()
            set(value) {
                if (value !in operatorIDTypeRange)
                    throw OpenDroneIDException("Operator ID Type range error")
                data[0] = value.toByte()
            }

        val operatorIDMaxLength = 20
        var operatorID: String
            get() = String(data.sliceArray(1..operatorIDMaxLength)).trim(0x00.toChar())
            set(value) {
                if (operatorIDMaxLength < value.length)
                    throw OpenDroneIDException("Operator ID length error")
                data = byteArrayOf(data[0]) + value.toByteArray() +
                        ByteArray(operatorIDMaxLength - value.length) + ByteArray(3)
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }
    }

    @ExperimentalUnsignedTypes
    class MessagePack(data: ByteArray = byteArrayOf(0x19, 0x00)) : MessageData(data) {
        companion object {
            const val SIZE = 252
        }

        override var maxSize = SIZE

        override var dataRange = 2..SIZE

        var messageSize: Int
            get() = data[0].toUByte().toInt()
            set(value) {
                data[0] = 0x19.toByte()
            }

        val messageNumberRange = 0..10
        var messageNumber: Int
            get() = data[1].toUByte().toInt()
            set(value) {
                if (value !in messageNumberRange)
                    throw OpenDroneIDException("Message Number length error")
                data[1] = value.toByte()
            }

        var messages: Array<Message> = arrayOf()
            get() {
                val number = (data.size - 2) / Message.SIZE
                return Array(number) { i ->
                    Message(data.sliceArray((Message.SIZE * i + 2) until (Message.SIZE * (i + 1) + 2)))
                }
            }
            set(value) {
                field = value
                messageNumber = value.size
                data = byteArrayOf(data[0]) + byteArrayOf(data[1])
                for (msg in value) {
                    data += msg.toByteArray()
                }
            }

        init {
            if (data.size !in dataRange)
                throw OpenDroneIDException("Data length error")
        }
    }

    class AuthBuilder {
        companion object {
            const val PAGE = 5
        }

        private var auths: Array<Authentication> = Array(PAGE) { i ->
            when (i) {
                0 -> AuthenticationHeader()
                else -> AuthenticationAdditional()
            }
        }

        var messages: Array<Message> = arrayOf()
            get() = Array((auths[0] as AuthenticationHeader).pageCount) { i -> Message(auths[i]) }
            private set

        var authType: AuthType
            get() = (auths[0] as AuthenticationHeader).authType
            set(value) {
                for (msg in auths) msg.authType = value
            }

        val unixTimeRange = 1546300800..3693752040
        var unixTime: Long
            get() = (auths[0] as AuthenticationHeader).unixTime
            set(value) {
                if (value !in unixTimeRange)
                    throw OpenDroneIDException("UnixTime range error")
                (auths[0] as AuthenticationHeader).unixTime = value
            }
        var authData: ByteArray
            get() {
                var array = ByteArray(0)
                val count = (auths[0] as AuthenticationHeader).pageCount
                for (i in 0 until count) {
                    if (auths[i].pageNumber != i)
                        throw OpenDroneIDException("Auth Page Number illegal")
                    array += auths[i].authData
                }
                return array
            }
            set(value) {
                var size = value.size
                for (i in 0 until PAGE) {
                    if (size <= 0) break
                    when (i) {
                        0 -> auths[0] = AuthenticationHeader().apply {
                            authType = auths[0].authType
                            pageNumber = i
                            pageCount = when (value.size) {
                                in 0..17 -> 1
                                in 18..41 -> 2
                                in 42..65 -> 3
                                in 66..85 -> 4
                                in 86..109 -> 5
                                else -> 0
                            }
                            length = value.size
                            unixTime = (auths[0] as AuthenticationHeader).unixTime
                            authData = value.copyOf(AuthenticationHeader.AuthDataLength)
                            size -= AuthenticationHeader.AuthDataLength
                        }
                        else -> {
                            val start = AuthenticationHeader.AuthDataLength +
                                    (AuthenticationHeader.AuthDataLength * (i - 1))
                            val end = start + kotlin.math.min(
                                AuthenticationHeader.AuthDataLength,
                                size
                            )
                            val length = end - start

                            auths[i] = AuthenticationAdditional().apply {
                                authType = auths[i].authType
                                pageNumber = i
                                authData =
                                    value.sliceArray(start until end) +
                                            ByteArray(AuthenticationHeader.AuthDataLength - length)
                            }
                            size -= AuthenticationHeader.AuthDataLength
                        }
                    }
                }
            }

        fun add(auth: Authentication) {
            auths[auth.pageNumber] = auth
        }
    }
}

