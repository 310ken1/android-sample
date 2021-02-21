package com.example.sample.astm.f3411

import com.example.sample.astm.f3411.OpenDroneIDMessage.AuthType
import com.example.sample.astm.f3411.OpenDroneIDMessage.DirectionSegment
import com.example.sample.astm.f3411.OpenDroneIDMessage.HeightType
import com.example.sample.astm.f3411.OpenDroneIDMessage.HorizontalAccuracy
import com.example.sample.astm.f3411.OpenDroneIDMessage.IDType
import com.example.sample.astm.f3411.OpenDroneIDMessage.MessageType
import com.example.sample.astm.f3411.OpenDroneIDMessage.OperationalStatus
import com.example.sample.astm.f3411.OpenDroneIDMessage.SpeedAccuracy
import com.example.sample.astm.f3411.OpenDroneIDMessage.SpeedMultiplier
import com.example.sample.astm.f3411.OpenDroneIDMessage.UAType
import com.example.sample.astm.f3411.OpenDroneIDMessage.VerticalAccuracy
import org.junit.Assert
import org.junit.Test
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class ByteExtensionTest {
    companion object {
        data class BitGetPattern(val expected: Int, val byte: Int, val bit: Int)
        data class BitSetPattern(val expected: Int, val byte: Int, val bit: Int, val add: Int)

        @DataPoints
        @JvmField
        val bitGetPattern = arrayOf(
            BitGetPattern(1, 0b00000001, 0),
            BitGetPattern(1, 0b00000010, 1),
            BitGetPattern(1, 0b00000100, 2),
            BitGetPattern(1, 0b00001000, 3),
            BitGetPattern(1, 0b00010000, 4),
            BitGetPattern(1, 0b00100000, 5),
            BitGetPattern(1, 0b01000000, 6),
            BitGetPattern(1, 0b10000000, 7),
            BitGetPattern(0, 0b11111110, 0),
            BitGetPattern(0, 0b01111111, 7),
        )

        @DataPoints
        @JvmField
        val bitSetPattern = arrayOf(
            BitSetPattern(0b00000001, 0b00000000, 0, 1),
            BitSetPattern(0b10000000, 0b00000000, 7, 1),
            BitSetPattern(0b11111110, 0b11111111, 0, 0),
            BitSetPattern(0b01111111, 0b11111111, 7, 0),
        )
    }

    @Theory
    fun bitGet(p: BitGetPattern) {
        Assert.assertEquals(p.expected, p.byte.toByte().bit(p.bit))
    }

    @Test
    fun bitGetException() {
        try {
            1.toByte().bit(-1)
            Assert.fail()
        } catch (e: Exception) {
        }
        try {
            1.toByte().bit(8)
            Assert.fail()
        } catch (e: Exception) {
        }
    }

    @Theory
    fun bitSet(p: BitSetPattern) {
        Assert.assertEquals(p.expected.toByte(), p.byte.toByte().bit(p.bit, p.add))
    }

    @Test
    fun bitSetException() {
        try {
            1.toByte().bit(-1, 1)
            Assert.fail()
        } catch (e: Exception) {
        }
        try {
            1.toByte().bit(8, 1)
            Assert.fail()
        } catch (e: Exception) {
        }
        try {
            1.toByte().bit(0, -1)
            Assert.fail()
        } catch (e: Exception) {
        }
        try {
            1.toByte().bit(0, 2)
            Assert.fail()
        } catch (e: Exception) {
        }
    }

    @Test
    fun top4bitGet() {
        Assert.assertEquals(0b0110.toByte(), 0b01101001.toByte().top4bit())
    }

    @Test
    fun top4bitSet() {
        Assert.assertEquals(0b10011001.toByte(), 0b01101001.toByte().top4bit(0b1001))
    }

    @Test
    fun lower4bitGet() {
        Assert.assertEquals(0b1001.toByte(), 0b01101001.toByte().lower4bit())
    }

    @Test
    fun lower4bitSet() {
        Assert.assertEquals(0b01100110.toByte(), 0b01101001.toByte().lower4bit(0b0110))
    }
}

@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class HeaderTest {
    companion object {
        data class ParsePattern(
            val expectedType: MessageType,
            val expectedVersion: Int,
            val header: Int,
        )

        data class ParseInvalidPattern(val header: Int)
        data class BuildPattern(val expected: Int, val type: MessageType, val version: Int)

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
            ParsePattern(MessageType.BasicID, 0, 0x00),
            ParsePattern(MessageType.LocationVector, 0, 0x10),
            ParsePattern(MessageType.Authentication, 0, 0x20),
            ParsePattern(MessageType.SelfID, 0, 0x30),
            ParsePattern(MessageType.System, 0, 0x40),
            ParsePattern(MessageType.OperatorID, 0, 0x50),
            ParsePattern(MessageType.MessagePack, 0, 0xF0),
            ParsePattern(MessageType.BasicID, 0, 0x00),
            ParsePattern(MessageType.BasicID, 15, 0x0F),
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
            ParseInvalidPattern(0x60),
            ParseInvalidPattern(0x70),
            ParseInvalidPattern(0x80),
            ParseInvalidPattern(0x90),
            ParseInvalidPattern(0xA0),
            ParseInvalidPattern(0xB0),
            ParseInvalidPattern(0xC0),
            ParseInvalidPattern(0xD0),
            ParseInvalidPattern(0xE0),
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
            BuildPattern(0x00, MessageType.BasicID, 0),
            BuildPattern(0x10, MessageType.LocationVector, 0),
            BuildPattern(0x20, MessageType.Authentication, 0),
            BuildPattern(0x30, MessageType.SelfID, 0),
            BuildPattern(0x40, MessageType.System, 0),
            BuildPattern(0x50, MessageType.OperatorID, 0),
            BuildPattern(0xF0, MessageType.MessagePack, 0),
            BuildPattern(0x0F, MessageType.BasicID, 15),
        )
    }

    @Theory
    fun parse(p: ParsePattern) {
        val message = ByteArray(24) { 0x00 }
        val msg = byteArrayOf(p.header.toByte()) + message
        OpenDroneIDMessage.parse(msg).let {
            Assert.assertEquals(p.expectedType, it.header.type)
            Assert.assertEquals(p.expectedVersion, it.header.version)
        }
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
        val message = ByteArray(24) { 0x00 }
        val invalid = byteArrayOf(p.header.toByte()) + message
        try {
            OpenDroneIDMessage.parse(invalid)
            Assert.fail()
        } catch (e: OpenDroneIDMessage.OpenDroneIDException) {
            Assert.assertEquals("Unsupported message type", e.message)
        }
    }

    @Theory
    fun build(p: BuildPattern) {
        OpenDroneIDMessage.Message().apply {
            header.type = p.type
            header.version = p.version
        }.let {
            Assert.assertEquals(p.expected.toByte(), it.header.data)
        }
    }
}

@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class BasicIDTest {
    companion object {
        data class ParsePattern(
            val expectedIDType: IDType = IDType.None,
            val expectedUAType: UAType = UAType.None,
            val expectedUasID: String = "",
            val data: ByteArray,
        )

        data class BuildPattern(
            val expectedData: ByteArray,
            val uaType: UAType,
            val uasID: OpenDroneIDMessage.UasID,
        )

        data class ParseInvalidPattern(val expectedMsg: String, val data: ByteArray)
        data class BuildInvalidPattern(val expectedMsg: String, val uasID: OpenDroneIDMessage.UasID)

        // @formatter:off
        // IDType
        val None = byteArrayOf(
            0x00,                                                        // header
            0x00,                                                        // IDType/UAType
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // UAS ID
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00                                             // Reserved
        )
        val SerialNumber = byteArrayOf(
            0x00,                                                        // header
            0x10,                                                        // IDType/UAType
            0x49, 0x4E, 0x54, 0x43, 0x4A, 0x31, 0x32, 0x33, 0x2D, 0x34,  // UAS ID
            0x35, 0x36, 0x37, 0x2D, 0x38, 0x39, 0x30, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00                                             // Reserved                                 // Reserved
        )
        val RegistrationID = byteArrayOf(
            0x00,                                                        // header
            0x20,                                                        // IDType/UAType
            0x46, 0x41, 0x31, 0x32, 0x33, 0x34, 0x35, 0x38, 0x39, 0x37,  // UAS ID
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00                                             // Reserved                                        // Reserved
        )
        val UUID = byteArrayOf(
            0x00,                                                        // header
            0x30,                                                        // IDType/UAType
            0x55, 0x0E, 0x84.toByte(), 0x00, 0xe2.toByte(),              // UAS ID
            0x9b.toByte(), 0x41, 0xD4.toByte(), 0xA7.toByte(), 0x16,
            0x44, 0x66, 0x55, 0x44, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00                                             // Reserved
        )
        val InvalidIDType04 = byteArrayOf(0x00, 0x40) + ByteArray(23) {0x00}
        val InvalidIDType05 = byteArrayOf(0x00, 0x50) + ByteArray(23) {0x00}
        val InvalidIDType06 = byteArrayOf(0x00, 0x60) + ByteArray(23) {0x00}
        val InvalidIDType07 = byteArrayOf(0x00, 0x70) + ByteArray(23) {0x00}
        val InvalidIDType08 = byteArrayOf(0x00, 0x80.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType09 = byteArrayOf(0x00, 0x90.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0A = byteArrayOf(0x00, 0xA0.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0B = byteArrayOf(0x00, 0xB0.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0C = byteArrayOf(0x00, 0xC0.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0D = byteArrayOf(0x00, 0xD0.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0E = byteArrayOf(0x00, 0xE0.toByte()) + ByteArray(23) {0x00}
        val InvalidIDType0F = byteArrayOf(0x00, 0xF0.toByte()) + ByteArray(23) {0x00}
        // UAType
        val Aeroplane = byteArrayOf(0x00, 0x01) + ByteArray(23) {0x00}
        val Helicopter = byteArrayOf(0x00, 0x02) + ByteArray(23) {0x00}
        val Gyroplane = byteArrayOf(0x00, 0x03) + ByteArray(23) {0x00}
        val HybridLift = byteArrayOf(0x00, 0x04) + ByteArray(23) {0x00}
        val Ornithopter = byteArrayOf(0x00, 0x05) + ByteArray(23) {0x00}
        val Glider = byteArrayOf(0x00, 0x06) + ByteArray(23) {0x00}
        val Kite = byteArrayOf(0x00, 0x07) + ByteArray(23) {0x00}
        val FreeBalloon = byteArrayOf(0x00, 0x08) + ByteArray(23) {0x00}
        val CaptiveBalloon = byteArrayOf(0x00, 0x09) + ByteArray(23) {0x00}
        val Airship = byteArrayOf(0x00, 0x0A) + ByteArray(23) {0x00}
        val FreeFall = byteArrayOf(0x00, 0x0B) + ByteArray(23) {0x00}
        val Rocket = byteArrayOf(0x00, 0x0C) + ByteArray(23) {0x00}
        val TetheredPoweredAircraft = byteArrayOf(0x00, 0x0D) + ByteArray(23) {0x00}
        val GroundObstacle = byteArrayOf(0x00, 0x0E) + ByteArray(23) {0x00}
        val Other = byteArrayOf(0x00, 0x0F) + ByteArray(23) {0x00}
        // UasID
        val NoneNullString = byteArrayOf(0x00, 0x00) + ByteArray(23) {0x00}
        val SerialNumberNullString = byteArrayOf(0x00, 0x10) + ByteArray(23) {0x00}
        val RegistrationIDNullString = byteArrayOf(0x00, 0x20) + ByteArray(23) {0x00}
        val UUIDNullString = byteArrayOf(0x00, 0x30) + ByteArray(23) {0x00}
        val NoneMaxString = byteArrayOf(0x00, 0x00) + ByteArray(20) {0x41} + ByteArray(3) {0x00}
        val SerialNumberMaxString = byteArrayOf(0x00, 0x10)  + ByteArray(20) {0x41} + ByteArray(3) {0x00}
        val RegistrationIDMaxString = byteArrayOf(0x00, 0x20)  + ByteArray(20) {0x41} + ByteArray(3) {0x00}
        val UUIDMaxString = byteArrayOf(0x00, 0x30)  + ByteArray(20) {0x41} + ByteArray(3) {0x00}
        val InvalidDataSizeUnder = ByteArray(24) {0x00}
        val InvalidDataSizeOver = ByteArray(26) {0x00}

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
        /* 00 */ ParsePattern(IDType.None, UAType.None, "", None),
        /* 01 */ ParsePattern(IDType.SerialNumber, UAType.None, "INTCJ123-4567-890", SerialNumber),
        /* 02 */ ParsePattern(IDType.RegistrationID, UAType.None, "FA12345897", RegistrationID),
        /* 03 */ ParsePattern(IDType.UUID, UAType.None, "550e8400e29b41d4a716446655440000", UUID),
        /* 04 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType04),
        /* 05 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType05),
        /* 06 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType06),
        /* 07 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType07),
        /* 08 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType08),
        /* 09 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType09),
        /* 10 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0A),
        /* 11 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0B),
        /* 12 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0C),
        /* 13 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0D),
        /* 14 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0E),
        /* 15 */ ParsePattern(expectedIDType=IDType.Invalid, data=InvalidIDType0F),
        /* 16 */ ParsePattern(expectedUAType=UAType.Aeroplane, data=Aeroplane),
        /* 17 */ ParsePattern(expectedUAType=UAType.Helicopter, data=Helicopter),
        /* 18 */ ParsePattern(expectedUAType=UAType.Gyroplane, data=Gyroplane),
        /* 19 */ ParsePattern(expectedUAType=UAType.HybridLift, data=HybridLift),
        /* 20 */ ParsePattern(expectedUAType=UAType.Ornithopter, data=Ornithopter),
        /* 21 */ ParsePattern(expectedUAType=UAType.Glider, data=Glider),
        /* 22 */ ParsePattern(expectedUAType=UAType.Kite, data=Kite),
        /* 23 */ ParsePattern(expectedUAType=UAType.FreeBalloon, data=FreeBalloon),
        /* 24 */ ParsePattern(expectedUAType=UAType.CaptiveBalloon, data=CaptiveBalloon),
        /* 25 */ ParsePattern(expectedUAType=UAType.Airship, data=Airship),
        /* 26 */ ParsePattern(expectedUAType=UAType.FreeFall, data=FreeFall),
        /* 27 */ ParsePattern(expectedUAType=UAType.Rocket, data=Rocket),
        /* 28 */ ParsePattern(expectedUAType=UAType.TetheredPoweredAircraft, data=TetheredPoweredAircraft),
        /* 29 */ ParsePattern(expectedUAType=UAType.GroundObstacle, data=GroundObstacle),
        /* 30 */ ParsePattern(expectedUAType=UAType.Other, data=Other),
        /* 31 */ ParsePattern(data=NoneNullString),
        /* 32 */ ParsePattern(expectedIDType=IDType.SerialNumber, data=SerialNumberNullString),
        /* 33 */ ParsePattern(expectedIDType=IDType.RegistrationID, data=RegistrationIDNullString),
        /* 34 */ ParsePattern(IDType.UUID, UAType.None, "00000000000000000000000000000000", UUIDNullString),
        /* 35 */ ParsePattern(expectedIDType=IDType.None, data=NoneMaxString),
        /* 36 */ ParsePattern(IDType.SerialNumber, UAType.None, "AAAAAAAAAAAAAAAAAAAA", SerialNumberMaxString),
        /* 37 */ ParsePattern(IDType.RegistrationID, UAType.None, "AAAAAAAAAAAAAAAAAAAA", RegistrationIDMaxString),
        /* 38 */ ParsePattern(IDType.UUID, UAType.None, "41414141414141414141414141414141", UUIDMaxString),
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
        /* 00 */ ParseInvalidPattern("Data length error",InvalidDataSizeUnder),
        /* 01 */ ParseInvalidPattern("Data length error",InvalidDataSizeOver),
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
        /* 00 */ BuildPattern(None, UAType.None, OpenDroneIDMessage.NoneID()),
        /* 01 */ BuildPattern(SerialNumber, UAType.None, OpenDroneIDMessage.SerialNumber("INTCJ123-4567-890")),
        /* 02 */ BuildPattern(RegistrationID, UAType.None, OpenDroneIDMessage.RegistrationID("FA12345897")),
        /* 03 */ BuildPattern(UUID, UAType.None, OpenDroneIDMessage.UUID("550e8400e29b41d4a716446655440000")),
        /* 04 */ BuildPattern(Aeroplane, UAType.Aeroplane, OpenDroneIDMessage.NoneID()),
        /* 05 */ BuildPattern(Helicopter, UAType.Helicopter, OpenDroneIDMessage.NoneID()),
        /* 06 */ BuildPattern(Gyroplane, UAType.Gyroplane, OpenDroneIDMessage.NoneID()),
        /* 07 */ BuildPattern(HybridLift, UAType.HybridLift, OpenDroneIDMessage.NoneID()),
        /* 08 */ BuildPattern(Ornithopter, UAType.Ornithopter, OpenDroneIDMessage.NoneID()),
        /* 09 */ BuildPattern(Glider, UAType.Glider, OpenDroneIDMessage.NoneID()),
        /* 10 */ BuildPattern(Kite, UAType.Kite, OpenDroneIDMessage.NoneID()),
        /* 11 */ BuildPattern(FreeBalloon, UAType.FreeBalloon, OpenDroneIDMessage.NoneID()),
        /* 12 */ BuildPattern(CaptiveBalloon, UAType.CaptiveBalloon, OpenDroneIDMessage.NoneID()),
        /* 13 */ BuildPattern(Airship, UAType.Airship, OpenDroneIDMessage.NoneID()),
        /* 14 */ BuildPattern(FreeFall, UAType.FreeFall, OpenDroneIDMessage.NoneID()),
        /* 15 */ BuildPattern(Rocket, UAType.Rocket, OpenDroneIDMessage.NoneID()),
        /* 16 */ BuildPattern(TetheredPoweredAircraft, UAType.TetheredPoweredAircraft, OpenDroneIDMessage.NoneID()),
        /* 17 */ BuildPattern(GroundObstacle, UAType.GroundObstacle, OpenDroneIDMessage.NoneID()),
        /* 18 */ BuildPattern(Other, UAType.Other, OpenDroneIDMessage.NoneID()),
        )

        @DataPoints
        @JvmField
        val buildInvalidPattern = arrayOf(
        /* 00 */ BuildInvalidPattern("UAS ID length error", OpenDroneIDMessage.NoneID("0123456789012345678901234")),
        /* 01 */ BuildInvalidPattern("UAS ID length error", OpenDroneIDMessage.SerialNumber("0123456789012345678901234")),
        /* 02 */ BuildInvalidPattern("UAS ID length error", OpenDroneIDMessage.RegistrationID("0123456789012345678901234")),
        /* 03 */ BuildInvalidPattern("UAS ID length error", OpenDroneIDMessage.UUID("001122334455667788990011223344556677889900")),
        )
        // @formatter:on
    }

    @Theory
    fun parse(p: ParsePattern) {
        (OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.BasicID).let {
            Assert.assertEquals(p.expectedIDType, it.idType)
            Assert.assertEquals(p.expectedUAType, it.uaType)
            Assert.assertEquals(p.expectedUasID, it.uasID.toString())
        }
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
        try {
            val basicID = OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.BasicID
            basicID.idType
            basicID.uaType
            basicID.uasID
            Assert.fail()
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }

    @Theory
    fun build(p: BuildPattern) {
        OpenDroneIDMessage.Message(OpenDroneIDMessage.BasicID().apply {
            uaType = p.uaType
            uasID = p.uasID
        }).let {
            Assert.assertArrayEquals(p.expectedData, it.toByteArray())
        }
    }

    @Theory
    fun buildInvalid(p: BuildInvalidPattern) {
        try {
            OpenDroneIDMessage.Message(OpenDroneIDMessage.BasicID().apply {
                uasID = p.uasID
                Assert.fail()
            })
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }

    @Test
    fun uuid() {
        val odd = "001122334455667788990011223344556"
        try {
            OpenDroneIDMessage.UUID(odd)
            Assert.fail()
        } catch (e: Exception) {
            Assert.assertTrue(e is StringIndexOutOfBoundsException)
        }
    }
}

@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class LocationVectorTest {
    companion object {
        data class ParsePattern(
            val expectedStatus: OperationalStatus = OperationalStatus.Undeclared,
            val expectedHeightType: HeightType = HeightType.AboveTakeoff,
            val expectedDirectionSegment: DirectionSegment = DirectionSegment.East,
            val expectedSpeedMultiplier: SpeedMultiplier = SpeedMultiplier.X025,
            val expectedTrackDirection: Int = 0,
            val expectedSpeed: Double = 0.0,
            val expectedVerticalSpeed: Double = 0.0,
            val expectedLatitude: Double = 0.0,
            val expectedLongitude: Double = 0.0,
            val expectedPressureAltitude: Double = -1000.0,
            val expectedGeodeticAltitude: Double = -1000.0,
            val expectedHeight: Double = -1000.0,
            val expectedHorizontalAccuracy: HorizontalAccuracy = HorizontalAccuracy.Unknown,
            val expectedVerticalAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val expectedBaroAltitudeAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val expectedSpeedAccuracy: SpeedAccuracy = SpeedAccuracy.Unknown,
            val expectedTimestamp: Int = 0,
            val expectedTimestampAccuracy: Double = 0.0,
            val data: ByteArray,
        )

        data class ParseInvalidPattern(val expectedMsg: String, val data: ByteArray)
        data class BuildPattern(
            val expectedData: ByteArray,
            val status: OperationalStatus = OperationalStatus.Undeclared,
            val heightType: HeightType = HeightType.AboveTakeoff,
            val directionSegment: DirectionSegment = DirectionSegment.East,
            val speedMultiplier: SpeedMultiplier = SpeedMultiplier.X025,
            val trackDirection: Int = 0,
            val speed: Double = 0.0,
            val verticalSpeed: Double = 0.0,
            val latitude: Double = 0.0,
            val longitude: Double = 0.0,
            val pressureAltitude: Double = -1000.0,
            val geodeticAltitude: Double = -1000.0,
            val height: Double = -1000.0,
            val horizontalAccuracy: HorizontalAccuracy = HorizontalAccuracy.Unknown,
            val verticalAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val baroAltitudeAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val speedAccuracy: SpeedAccuracy = SpeedAccuracy.Unknown,
            val timestamp: Int = 0,
            val timestampAccuracy: Double = 0.0,
        )

        data class BuildInvalidPattern(
            val expectedMsg: String,
            val status: OperationalStatus = OperationalStatus.Undeclared,
            val heightType: HeightType = HeightType.AboveTakeoff,
            val directionSegment: DirectionSegment = DirectionSegment.East,
            val speedMultiplier: SpeedMultiplier = SpeedMultiplier.X025,
            val trackDirection: Int = 0,
            val speed: Double = 0.0,
            val verticalSpeed: Double = 0.0,
            val latitude: Double = 0.0,
            val longitude: Double = 0.0,
            val pressureAltitude: Double = -1000.0,
            val geodeticAltitude: Double = -1000.0,
            val height: Double = -1000.0,
            val horizontalAccuracy: HorizontalAccuracy = HorizontalAccuracy.Unknown,
            val verticalAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val baroAltitudeAccuracy: VerticalAccuracy = VerticalAccuracy.Unknown,
            val speedAccuracy: SpeedAccuracy = SpeedAccuracy.Unknown,
            val timestamp: Int = 0,
            val timestampAccuracy: Double = 0.0,
        )

        // @formatter:off
        // OperationalStatus
        val Undeclared = byteArrayOf(0x10, 0x00) + ByteArray(23) {0x00}
        val Ground = byteArrayOf(0x10, 0x10) + ByteArray(23) {0x00}
        val Airborne = byteArrayOf(0x10, 0x20) + ByteArray(23) {0x00}
        val Reserved03 = byteArrayOf(0x10, 0x30) + ByteArray(23) {0x00}
        val Reserved04 = byteArrayOf(0x10, 0x40) + ByteArray(23) {0x00}
        val Reserved05 = byteArrayOf(0x10, 0x50) + ByteArray(23) {0x00}
        val Reserved06 = byteArrayOf(0x10, 0x60) + ByteArray(23) {0x00}
        val Reserved07 = byteArrayOf(0x10, 0x70) + ByteArray(23) {0x00}
        val Reserved08 = byteArrayOf(0x10, 0x80.toByte()) + ByteArray(23) {0x00}
        val Reserved09 = byteArrayOf(0x10, 0x90.toByte()) + ByteArray(23) {0x00}
        val Reserved0A = byteArrayOf(0x10, 0xA0.toByte()) + ByteArray(23) {0x00}
        val Reserved0B = byteArrayOf(0x10, 0xB0.toByte()) + ByteArray(23) {0x00}
        val Reserved0C = byteArrayOf(0x10, 0xC0.toByte()) + ByteArray(23) {0x00}
        val Reserved0D = byteArrayOf(0x10, 0xD0.toByte()) + ByteArray(23) {0x00}
        val Reserved0E = byteArrayOf(0x10, 0xE0.toByte()) + ByteArray(23) {0x00}
        val Reserved0F = byteArrayOf(0x10, 0xF0.toByte()) + ByteArray(23) {0x00}
        // HeightType
        val AboveTakeoff = byteArrayOf(0x10, 0x00) + ByteArray(23) {0x00}
        val AGL = byteArrayOf(0x10, 0x04) + ByteArray(23) {0x00}
        // DirectionSegment
        val East = byteArrayOf(0x10, 0x00) + ByteArray(23) {0x00}
        val West = byteArrayOf(0x10, 0x02) + ByteArray(23) {0x00}
        // SpeedMultiplier
        val X025 = byteArrayOf(0x10, 0x00) + ByteArray(23) {0x00}
        val X075 = byteArrayOf(0x10, 0x01) + ByteArray(23) {0x00}
        // TrackDirection
        val TrackDirectionEastMin = byteArrayOf(0x10, 0x00, 0x00) + ByteArray(22) {0x00}
        val TrackDirectionEastMax = byteArrayOf(0x10, 0x00, 0xB3.toByte()) + ByteArray(22) {0x00}
        val TrackDirectionEastOver = byteArrayOf(0x10, 0x00, 0xB4.toByte()) + ByteArray(22) {0x00}
        val TrackDirectionWestMin = byteArrayOf(0x10, 0x02, 0x00) + ByteArray(22) {0x00}
        val TrackDirectionWestMax = byteArrayOf(0x10, 0x02, 0xB3.toByte()) + ByteArray(22) {0x00}
        val TrackDirectionWestOver = byteArrayOf(0x10, 0x02, 0xB4.toByte()) + ByteArray(22) {0x00}
        val TrackDirectionUnknown = byteArrayOf(0x10, 0x02, 0xB5.toByte()) + ByteArray(22) {0x00}
        // Speed
        val SpeedMin = byteArrayOf(0x10, 0x00, 0x00, 0x00) + ByteArray(21) {0x00}
        val SpeedX025End = byteArrayOf(0x10, 0x00, 0x00, 0xE1.toByte()) + ByteArray(21) {0x00}
        val SpeedX075Start = byteArrayOf(0x10, 0x01, 0x00, 0x00) + ByteArray(21) {0x00}
        val SpeedMax = byteArrayOf(0x10, 0x01, 0x00, 0xFE.toByte()) + ByteArray(21) {0x00}
        val SpeedOver = byteArrayOf(0x10, 0x00, 0x00, 0xFF.toByte()) + ByteArray(21) {0x00}
        val SpeedUnknown = byteArrayOf(0x10, 0x01, 0x00, 0xFF.toByte()) + ByteArray(21) {0x00}
        // VerticalSpeed
        val VerticalSpeedUnder = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x83.toByte()) + ByteArray(20) {0x00}
        val VerticalSpeedMin = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x84.toByte()) + ByteArray(20) {0x00}
        val VerticalSpeedMax = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x7C) + ByteArray(20) {0x00}
        val VerticalSpeedOver = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x7D) + ByteArray(20) {0x00}
        val VerticalSpeedUnknown = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x7E) + ByteArray(20) {0x00}
        // Latitude
        val LatitudeUnder = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0xFF.toByte(), 0x16, 0x5B, 0xCA.toByte()) + ByteArray(16) {0x00}
        val LatitudeMin = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x17, 0x5B, 0xCA.toByte()) + ByteArray(16) {0x00}
        val LatitudeMax = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0x00, 0xE9.toByte(), 0xA4.toByte(), 0x35) + ByteArray(16) {0x00}
        val LatitudeOver = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0x01, 0xE9.toByte(), 0xA4.toByte(), 0x35) + ByteArray(16) {0x00}
        val LatitudeUnknown = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00) + ByteArray(16) {0x00}
        val LatitudeRepeatingDecimal = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00,
            0x15, 0xCD.toByte(), 0x5B, 0x07) + ByteArray(16) {0x00}
        // Longitude
        val longitudeUnder = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0xFF.toByte(), 0x1E, 0xB6.toByte(), 0x94.toByte()) + ByteArray(12) {0x00}
        val longitudeMin = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x2E, 0xB6.toByte(), 0x94.toByte()) + ByteArray(12) {0x00}
        val longitudeMax = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0xD2.toByte(), 0x49, 0x6B) + ByteArray(12) {0x00}
        val longitudeOver = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0xD2.toByte(), 0x49, 0x6B) + ByteArray(12) {0x00}
        val longitudeUnknown = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00) + ByteArray(12) {0x00}
        val longitudeRepeatingDecimal = byteArrayOf(0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x15, 0xCD.toByte(), 0x5B, 0x07) + ByteArray(12) {0x00}
        // PressureAltitude
        val PressureAltitudeMin = byteArrayOf(0x10) + ByteArray(12) {0x00} +
            byteArrayOf(0x01, 0x00) + ByteArray(10) {0x00}
        val PressureAltitudeMax = byteArrayOf(0x10) + ByteArray(12) {0x00} +
            byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + ByteArray(10) {0x00}
        val PressureAltitudeOver = byteArrayOf(0x10) + ByteArray(12) {0x00} +
            byteArrayOf(0xFF.toByte(), 0xFF.toByte()) + ByteArray(10) {0x00}
        val PressureAltitudeUnknown = byteArrayOf(0x10) + ByteArray(12) {0x00} +
            byteArrayOf(0x00, 0x00) + ByteArray(10) {0x00}
        // GeodeticAltitude
        val GeodeticAltitudeMin = byteArrayOf(0x10) + ByteArray(14) {0x00} +
            byteArrayOf(0x01, 0x00) + ByteArray(8) {0x00}
        val GeodeticAltitudeMax = byteArrayOf(0x10) + ByteArray(14) {0x00} +
            byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + ByteArray(8) {0x00}
        val GeodeticAltitudeOver = byteArrayOf(0x10) + ByteArray(14) {0x00} +
            byteArrayOf(0xFF.toByte(), 0xFF.toByte()) + ByteArray(8) {0x00}
        val GeodeticAltitudeUnknown = byteArrayOf(0x10) + ByteArray(14) {0x00} +
            byteArrayOf(0x00, 0x00) + ByteArray(8) {0x00}
        // Height
        val HeightMin = byteArrayOf(0x10) + ByteArray(16) {0x00} +
            byteArrayOf(0x01, 0x00) + ByteArray(6) {0x00}
        val HeightMax = byteArrayOf(0x10) + ByteArray(16) {0x00} +
            byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + ByteArray(6) {0x00}
        val HeightOver = byteArrayOf(0x10) + ByteArray(16) {0x00} +
            byteArrayOf(0xFF.toByte(), 0xFF.toByte()) + ByteArray(6) {0x00}
        val HeightUnknown = byteArrayOf(0x10) + ByteArray(16) {0x00} +
            byteArrayOf(0x00, 0x00) + ByteArray(6) {0x00}
        // HorizontalAccuracy
        val HorizontalAccuracyUnknown = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x00) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc18520m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x10) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc7408m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x20) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc3704m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x30) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc1852m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x40) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc926m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x50) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc555dp6m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x60) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc185dp2m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x70) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc92dp6m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x80.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc30m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x90.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc10m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xA0.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc3m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xB0.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracyAcc1m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xC0.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracy0D = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xD0.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracy0E = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xE0.toByte()) + ByteArray(5) {0x00}
        val HorizontalAccuracy0F = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0xF0.toByte()) + ByteArray(5) {0x00}
        // VerticalAccuracy
        val VerticalAccuracyUnknown = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x00.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc150m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x01.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc45m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x02.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc25m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x03.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc10m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x04.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc3m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x05.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracyAcc1m = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x06.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy07 = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x07.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy08 = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x08.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy09 = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x09.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0A = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0A.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0B = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0B.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0C = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0C.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0D = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0D.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0E = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0E.toByte()) + ByteArray(5) {0x00}
        val VerticalAccuracy0F = byteArrayOf(0x10) + ByteArray(18) {0x00} +
            byteArrayOf(0x0F.toByte()) + ByteArray(5) {0x00}
        // BaroAltitudeAccuracy
        val BaroAltitudeAccuracyUnknown = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x00.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc150m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x10.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc45m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x20.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc25m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x30.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc10m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x40.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc3m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x50.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracyAcc1m = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x60.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy07 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x70.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy08 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x80.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy09 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x90.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0A = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xB0.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0B = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xB0.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0C = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xC0.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0D = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xD0.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0E = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xE0.toByte()) + ByteArray(4) {0x00}
        val BaroAltitudeAccuracy0F = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0xF0.toByte()) + ByteArray(4) {0x00}
        // SpeedAccuracy
        val SpeedAccuracyUnknown = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x00.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracyAcc10mps = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x01.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracyAcc3mps = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x02.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracyAcc1mps = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x03.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracyAcc03mps = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x04.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy05 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x05.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy06 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x06.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy07 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x07.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy08 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x08.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy09 = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x09.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0A = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0A.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0B = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0B.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0C = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0C.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0D = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0D.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0E = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0E.toByte()) + ByteArray(4) {0x00}
        val SpeedAccuracy0F = byteArrayOf(0x10) + ByteArray(19) {0x00} +
            byteArrayOf(0x0F.toByte()) + ByteArray(4) {0x00}
        // Timestamp
        val TimestampMin = byteArrayOf(0x10) + ByteArray(20) {0x00} +
            byteArrayOf(0x00, 0x00) + ByteArray(2) {0x00}
        val TimestampMax = byteArrayOf(0x10) + ByteArray(20) {0x00} +
            byteArrayOf(0xA0.toByte(), 0x8C.toByte()) + ByteArray(2) {0x00}
        val TimestampOver = byteArrayOf(0x10) + ByteArray(20) {0x00} +
            byteArrayOf(0xA1.toByte(), 0x8C.toByte()) + ByteArray(2) {0x00}
        // TimestampAccuracy
        val TimestampAccuracyMin = byteArrayOf(0x10) + ByteArray(22) {0x00} +
            byteArrayOf(0x01) + ByteArray(1) {0x00}
        val TimestampAccuracyMax = byteArrayOf(0x10) + ByteArray(22) {0x00} +
            byteArrayOf(0x0F) + ByteArray(1) {0x00}
        val TimestampAccuracyUnknown = byteArrayOf(0x10) + ByteArray(22) {0x00} +
            byteArrayOf(0x00) + ByteArray(1) {0x00}
        val TimestampAccuracyRepeatingDecimal = byteArrayOf(0x10) + ByteArray(22) {0x00} +
            byteArrayOf(0x09) + ByteArray(1) {0x00}
        // Invalid
        val InvalidDataSizeUnder = byteArrayOf(0x10) + ByteArray(23) {0x00}
        val InvalidDataSizeOver = byteArrayOf(0x10) + ByteArray(25) {0x00}

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
        /* 000 */ ParsePattern(expectedStatus=OperationalStatus.Undeclared, data=Undeclared),
        /* 001 */ ParsePattern(expectedStatus=OperationalStatus.Ground, data=Ground),
        /* 002 */ ParsePattern(expectedStatus=OperationalStatus.Airborne, data=Airborne),
        /* 003 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved03),
        /* 004 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved04),
        /* 005 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved05),
        /* 006 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved06),
        /* 007 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved07),
        /* 008 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved08),
        /* 009 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved09),
        /* 010 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0A),
        /* 011 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0B),
        /* 012 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0C),
        /* 013 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0D),
        /* 014 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0E),
        /* 015 */ ParsePattern(expectedStatus=OperationalStatus.Reserved, data=Reserved0F),
        /* 016 */ ParsePattern(expectedHeightType=HeightType.AboveTakeoff, data=AboveTakeoff),
        /* 017 */ ParsePattern(expectedHeightType=HeightType.AGL, data=AGL),
        /* 018 */ ParsePattern(expectedDirectionSegment=DirectionSegment.East, data=East),
        /* 019 */ ParsePattern(expectedDirectionSegment=DirectionSegment.West, data=West),
        /* 020 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X025, data=X025),
        /* 021 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X075, expectedSpeed=63.75, data=X075),
        /* 022 */ ParsePattern(expectedTrackDirection=0, data=TrackDirectionEastMin),
        /* 023 */ ParsePattern(expectedTrackDirection=179, data=TrackDirectionEastMax),
        /* 024 */ ParsePattern(expectedTrackDirection=361, data=TrackDirectionEastOver),
        /* 025 */ ParsePattern(expectedDirectionSegment=DirectionSegment.West, expectedTrackDirection=180, data=TrackDirectionWestMin),
        /* 026 */ ParsePattern(expectedDirectionSegment=DirectionSegment.West, expectedTrackDirection=359, data=TrackDirectionWestMax),
        /* 027 */ ParsePattern(expectedDirectionSegment=DirectionSegment.West, expectedTrackDirection=361, data=TrackDirectionWestOver),
        /* 028 */ ParsePattern(expectedDirectionSegment=DirectionSegment.West, expectedTrackDirection=361, data=TrackDirectionUnknown),
        /* 029 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X025, expectedSpeed=0.0, data=SpeedMin),
        /* 030 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X025, expectedSpeed=56.25, data=SpeedX025End),
        /* 031 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X075, expectedSpeed=56.50, data=SpeedX075Start),
        /* 032 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X075, expectedSpeed=254.25, data=SpeedMax),
        /* 033 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X025, expectedSpeed=255.0, data=SpeedOver),
        /* 034 */ ParsePattern(expectedSpeedMultiplier=SpeedMultiplier.X075, expectedSpeed=255.0, data=SpeedUnknown),
        /* 035 */ ParsePattern(expectedVerticalSpeed=63.0, data=VerticalSpeedUnder),
        /* 036 */ ParsePattern(expectedVerticalSpeed=-62.0, data=VerticalSpeedMin),
        /* 037 */ ParsePattern(expectedVerticalSpeed=62.0, data=VerticalSpeedMax),
        /* 038 */ ParsePattern(expectedVerticalSpeed=63.0, data=VerticalSpeedOver),
        /* 039 */ ParsePattern(expectedVerticalSpeed=63.0, data=VerticalSpeedUnknown),
        /* 040 */ ParsePattern(expectedLatitude=0.0, data=LatitudeUnder),
        /* 041 */ ParsePattern(expectedLatitude=-90.0, data=LatitudeMin),
        /* 042 */ ParsePattern(expectedLatitude=90.0, data=LatitudeMax),
        /* 043 */ ParsePattern(expectedLatitude=0.0, data=LatitudeOver),
        /* 044 */ ParsePattern(expectedLatitude=0.0, data=LatitudeUnknown),
        /* 045 */ ParsePattern(expectedLatitude=12.3456789, data=LatitudeRepeatingDecimal),
        /* 046 */ ParsePattern(expectedLongitude=0.0, data=longitudeUnder),
        /* 047 */ ParsePattern(expectedLongitude=-180.0, data=longitudeMin),
        /* 048 */ ParsePattern(expectedLongitude=180.0, data=longitudeMax),
        /* 049 */ ParsePattern(expectedLongitude=0.0, data=longitudeOver),
        /* 050 */ ParsePattern(expectedLongitude=0.0, data=longitudeUnknown),
        /* 051 */ ParsePattern(expectedLongitude=12.3456789, data=longitudeRepeatingDecimal),
        /* 052 */ ParsePattern(expectedPressureAltitude=-999.5, data=PressureAltitudeMin),
        /* 053 */ ParsePattern(expectedPressureAltitude=31767.0, data=PressureAltitudeMax),
        /* 054 */ ParsePattern(expectedPressureAltitude=-1000.0, data=PressureAltitudeOver),
        /* 055 */ ParsePattern(expectedPressureAltitude=-1000.0, data=PressureAltitudeUnknown),
        /* 056 */ ParsePattern(expectedGeodeticAltitude=-999.5, data=GeodeticAltitudeMin),
        /* 057 */ ParsePattern(expectedGeodeticAltitude=31767.0, data=GeodeticAltitudeMax),
        /* 058 */ ParsePattern(expectedGeodeticAltitude=-1000.0, data=GeodeticAltitudeOver),
        /* 059 */ ParsePattern(expectedGeodeticAltitude=-1000.0, data=GeodeticAltitudeUnknown),
        /* 060 */ ParsePattern(expectedHeight=-999.5, data=HeightMin),
        /* 061 */ ParsePattern(expectedHeight=31767.0, data=HeightMax),
        /* 062 */ ParsePattern(expectedHeight=-1000.0, data=HeightOver),
        /* 063 */ ParsePattern(expectedHeight=-1000.0, data=HeightUnknown),
        /* 064 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Unknown, data=HorizontalAccuracyUnknown),
        /* 065 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc18520m, data=HorizontalAccuracyAcc18520m),
        /* 066 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc7408m, data=HorizontalAccuracyAcc7408m),
        /* 067 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc3704m, data=HorizontalAccuracyAcc3704m),
        /* 068 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc1852m, data=HorizontalAccuracyAcc1852m),
        /* 069 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc926m, data=HorizontalAccuracyAcc926m),
        /* 070 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc555dp6m, data=HorizontalAccuracyAcc555dp6m),
        /* 071 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc185dp2m, data=HorizontalAccuracyAcc185dp2m),
        /* 072 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc92dp6m, data=HorizontalAccuracyAcc92dp6m),
        /* 073 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc30m, data=HorizontalAccuracyAcc30m),
        /* 074 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc10m, data=HorizontalAccuracyAcc10m),
        /* 075 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc3m, data=HorizontalAccuracyAcc3m),
        /* 076 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Acc1m, data=HorizontalAccuracyAcc1m),
        /* 077 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Reserved, data=HorizontalAccuracy0D),
        /* 078 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Reserved, data=HorizontalAccuracy0E),
        /* 079 */ ParsePattern(expectedHorizontalAccuracy=HorizontalAccuracy.Reserved, data=HorizontalAccuracy0F),
        /* 080 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Unknown, data=VerticalAccuracyUnknown),
        /* 081 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc150m, data=VerticalAccuracyAcc150m),
        /* 082 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc45m, data=VerticalAccuracyAcc45m),
        /* 083 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc25m, data=VerticalAccuracyAcc25m),
        /* 084 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc10m, data=VerticalAccuracyAcc10m),
        /* 085 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc3m, data=VerticalAccuracyAcc3m),
        /* 086 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Acc1m, data=VerticalAccuracyAcc1m),
        /* 087 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy07),
        /* 088 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy08),
        /* 089 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy09),
        /* 090 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0A),
        /* 091 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0B),
        /* 092 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0C),
        /* 093 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0D),
        /* 094 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0E),
        /* 095 */ ParsePattern(expectedVerticalAccuracy=VerticalAccuracy.Reserved, data=VerticalAccuracy0F),
        /* 096 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Unknown, data=BaroAltitudeAccuracyUnknown),
        /* 097 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc150m, data=BaroAltitudeAccuracyAcc150m),
        /* 098 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc45m, data=BaroAltitudeAccuracyAcc45m),
        /* 099 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc25m, data=BaroAltitudeAccuracyAcc25m),
        /* 100 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc10m, data=BaroAltitudeAccuracyAcc10m),
        /* 101 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc3m, data=BaroAltitudeAccuracyAcc3m),
        /* 102 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Acc1m, data=BaroAltitudeAccuracyAcc1m),
        /* 103 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy07),
        /* 104 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy08),
        /* 105 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy09),
        /* 106 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0A),
        /* 107 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0B),
        /* 108 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0C),
        /* 109 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0D),
        /* 110 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0E),
        /* 111 */ ParsePattern(expectedBaroAltitudeAccuracy=VerticalAccuracy.Reserved, data=BaroAltitudeAccuracy0F),
        /* 112 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Unknown, data=SpeedAccuracyUnknown),
        /* 113 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Acc10mps, data=SpeedAccuracyAcc10mps),
        /* 114 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Acc3mps, data=SpeedAccuracyAcc3mps),
        /* 115 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Acc1mps, data=SpeedAccuracyAcc1mps),
        /* 116 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Acc03mps, data=SpeedAccuracyAcc03mps),
        /* 117 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy05),
        /* 118 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy06),
        /* 119 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy07),
        /* 120 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy08),
        /* 121 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy09),
        /* 122 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0A),
        /* 123 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0B),
        /* 124 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0C),
        /* 125 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0D),
        /* 126 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0E),
        /* 127 */ ParsePattern(expectedSpeedAccuracy=SpeedAccuracy.Reserved, data=SpeedAccuracy0F),
        /* 128 */ ParsePattern(expectedTimestamp=0, data=TimestampMin),
        /* 129 */ ParsePattern(expectedTimestamp=36000, data=TimestampMax),
        /* 130 */ ParsePattern(expectedTimestamp=0, data=TimestampOver),
        /* 131 */ ParsePattern(expectedTimestampAccuracy=0.1, data=TimestampAccuracyMin),
        /* 132 */ ParsePattern(expectedTimestampAccuracy=1.5, data=TimestampAccuracyMax),
        /* 133 */ ParsePattern(expectedTimestampAccuracy=0.0, data=TimestampAccuracyUnknown),
        /* 134 */ ParsePattern(expectedTimestampAccuracy=0.9, data=TimestampAccuracyRepeatingDecimal),
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
        /* 00 */ ParseInvalidPattern("Data length error", InvalidDataSizeUnder),
        /* 01 */ ParseInvalidPattern("Data length error", InvalidDataSizeOver),
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
        /* 000 */ BuildPattern(Undeclared, OperationalStatus.Undeclared),
        /* 001 */ BuildPattern(Ground, OperationalStatus.Ground),
        /* 002 */ BuildPattern(Airborne, OperationalStatus.Airborne),
        /* 003 */ BuildPattern(AboveTakeoff, heightType=HeightType.AboveTakeoff),
        /* 004 */ BuildPattern(AGL, heightType=HeightType.AGL),
        /* 005 */ BuildPattern(TrackDirectionEastMin, trackDirection=0),
        /* 006 */ BuildPattern(TrackDirectionEastMax, trackDirection=179),
        /* 007 */ BuildPattern(TrackDirectionWestMin, trackDirection=180),
        /* 008 */ BuildPattern(TrackDirectionWestMax, trackDirection=359),
        /* 009 */ BuildPattern(TrackDirectionUnknown, trackDirection=OpenDroneIDMessage.LocationVector.TrackDirectionUnknown),
        /* 010 */ BuildPattern(SpeedMin, speed=0.0),
        /* 011 */ BuildPattern(SpeedX025End, speed=56.25),
        /* 012 */ BuildPattern(SpeedX075Start, speed=56.50),
        /* 013 */ BuildPattern(SpeedMax, speed=254.50),
        /* 014 */ BuildPattern(SpeedUnknown, speed=OpenDroneIDMessage.LocationVector.SpeedUnknown),
        /* 015 */ BuildPattern(VerticalSpeedMin, verticalSpeed=-62.0),
        /* 016 */ BuildPattern(VerticalSpeedMax, verticalSpeed=62.0),
        /* 017 */ BuildPattern(VerticalSpeedUnknown, verticalSpeed=OpenDroneIDMessage.LocationVector.VerticalSpeedUnknown),
        /* 018 */ BuildPattern(LatitudeMin, latitude=-90.0),
        /* 019 */ BuildPattern(LatitudeMax, latitude=90.0),
        /* 020 */ BuildPattern(LatitudeUnknown, latitude=OpenDroneIDMessage.LocationVector.LatitudeUnknown),
        /* 021 */ BuildPattern(LatitudeRepeatingDecimal, latitude=12.3456789),
        /* 022 */ BuildPattern(longitudeMin, longitude=-180.0),
        /* 023 */ BuildPattern(longitudeMax, longitude=180.0),
        /* 024 */ BuildPattern(longitudeUnknown, longitude=OpenDroneIDMessage.LocationVector.LongitudeUnknown),
        /* 025 */ BuildPattern(longitudeRepeatingDecimal, longitude=12.3456789),
        /* 026 */ BuildPattern(PressureAltitudeMin, pressureAltitude=-999.5),
        /* 027 */ BuildPattern(PressureAltitudeMax, pressureAltitude=31767.0),
        /* 028 */ BuildPattern(PressureAltitudeUnknown, pressureAltitude=OpenDroneIDMessage.LocationVector.AltitudeUnknown),
        /* 029 */ BuildPattern(GeodeticAltitudeMin, geodeticAltitude=-999.5),
        /* 030 */ BuildPattern(GeodeticAltitudeMax, geodeticAltitude=31767.0),
        /* 031 */ BuildPattern(GeodeticAltitudeUnknown, geodeticAltitude=OpenDroneIDMessage.LocationVector.AltitudeUnknown),
        /* 032 */ BuildPattern(HeightMin, height=-999.5),
        /* 033 */ BuildPattern(HeightMax, height=31767.0),
        /* 034 */ BuildPattern(HeightUnknown, height=OpenDroneIDMessage.LocationVector.AltitudeUnknown),
        /* 035 */ BuildPattern(HorizontalAccuracyUnknown, horizontalAccuracy=HorizontalAccuracy.Unknown),
        /* 036 */ BuildPattern(HorizontalAccuracyAcc18520m, horizontalAccuracy=HorizontalAccuracy.Acc18520m),
        /* 037 */ BuildPattern(HorizontalAccuracyAcc7408m, horizontalAccuracy=HorizontalAccuracy.Acc7408m),
        /* 038 */ BuildPattern(HorizontalAccuracyAcc3704m, horizontalAccuracy=HorizontalAccuracy.Acc3704m),
        /* 039 */ BuildPattern(HorizontalAccuracyAcc1852m, horizontalAccuracy=HorizontalAccuracy.Acc1852m),
        /* 040 */ BuildPattern(HorizontalAccuracyAcc926m, horizontalAccuracy=HorizontalAccuracy.Acc926m),
        /* 041 */ BuildPattern(HorizontalAccuracyAcc555dp6m, horizontalAccuracy=HorizontalAccuracy.Acc555dp6m),
        /* 042 */ BuildPattern(HorizontalAccuracyAcc185dp2m, horizontalAccuracy=HorizontalAccuracy.Acc185dp2m),
        /* 043 */ BuildPattern(HorizontalAccuracyAcc92dp6m, horizontalAccuracy=HorizontalAccuracy.Acc92dp6m),
        /* 044 */ BuildPattern(HorizontalAccuracyAcc30m, horizontalAccuracy=HorizontalAccuracy.Acc30m),
        /* 045 */ BuildPattern(HorizontalAccuracyAcc10m, horizontalAccuracy=HorizontalAccuracy.Acc10m),
        /* 046 */ BuildPattern(HorizontalAccuracyAcc3m, horizontalAccuracy=HorizontalAccuracy.Acc3m),
        /* 047 */ BuildPattern(HorizontalAccuracyAcc1m, horizontalAccuracy=HorizontalAccuracy.Acc1m),
        /* 048 */ BuildPattern(HorizontalAccuracy0F, horizontalAccuracy=HorizontalAccuracy.Reserved),
        /* 049 */ BuildPattern(VerticalAccuracyUnknown, verticalAccuracy=VerticalAccuracy.Unknown),
        /* 050 */ BuildPattern(VerticalAccuracyAcc150m, verticalAccuracy=VerticalAccuracy.Acc150m),
        /* 051 */ BuildPattern(VerticalAccuracyAcc45m, verticalAccuracy=VerticalAccuracy.Acc45m),
        /* 052 */ BuildPattern(VerticalAccuracyAcc25m, verticalAccuracy=VerticalAccuracy.Acc25m),
        /* 053 */ BuildPattern(VerticalAccuracyAcc10m, verticalAccuracy=VerticalAccuracy.Acc10m),
        /* 054 */ BuildPattern(VerticalAccuracyAcc3m, verticalAccuracy=VerticalAccuracy.Acc3m),
        /* 055 */ BuildPattern(VerticalAccuracyAcc1m, verticalAccuracy=VerticalAccuracy.Acc1m),
        /* 056 */ BuildPattern(VerticalAccuracy0F, verticalAccuracy=VerticalAccuracy.Reserved),
        /* 057 */ BuildPattern(BaroAltitudeAccuracyUnknown, baroAltitudeAccuracy=VerticalAccuracy.Unknown),
        /* 058 */ BuildPattern(BaroAltitudeAccuracyAcc150m, baroAltitudeAccuracy=VerticalAccuracy.Acc150m),
        /* 059 */ BuildPattern(BaroAltitudeAccuracyAcc45m, baroAltitudeAccuracy=VerticalAccuracy.Acc45m),
        /* 060 */ BuildPattern(BaroAltitudeAccuracyAcc25m, baroAltitudeAccuracy=VerticalAccuracy.Acc25m),
        /* 061 */ BuildPattern(BaroAltitudeAccuracyAcc10m, baroAltitudeAccuracy=VerticalAccuracy.Acc10m),
        /* 062 */ BuildPattern(BaroAltitudeAccuracyAcc3m, baroAltitudeAccuracy=VerticalAccuracy.Acc3m),
        /* 063 */ BuildPattern(BaroAltitudeAccuracyAcc1m, baroAltitudeAccuracy=VerticalAccuracy.Acc1m),
        /* 064 */ BuildPattern(BaroAltitudeAccuracy0F, baroAltitudeAccuracy=VerticalAccuracy.Reserved),
        /* 065 */ BuildPattern(SpeedAccuracyUnknown, speedAccuracy=SpeedAccuracy.Unknown),
        /* 066 */ BuildPattern(SpeedAccuracyAcc10mps, speedAccuracy=SpeedAccuracy.Acc10mps),
        /* 067 */ BuildPattern(SpeedAccuracyAcc3mps, speedAccuracy=SpeedAccuracy.Acc3mps),
        /* 068 */ BuildPattern(SpeedAccuracyAcc1mps, speedAccuracy=SpeedAccuracy.Acc1mps),
        /* 069 */ BuildPattern(SpeedAccuracyAcc03mps, speedAccuracy=SpeedAccuracy.Acc03mps),
        /* 070 */ BuildPattern(SpeedAccuracy0F, speedAccuracy=SpeedAccuracy.Reserved),
        /* 071 */ BuildPattern(TimestampMin, timestamp=0),
        /* 072 */ BuildPattern(TimestampMax, timestamp=36000),
        /* 073 */ BuildPattern(TimestampAccuracyMin, timestampAccuracy=0.1),
        /* 074 */ BuildPattern(TimestampAccuracyMax, timestampAccuracy=1.5),
        /* 075 */ BuildPattern(TimestampAccuracyUnknown, timestampAccuracy=0.0),
        /* 076 */ BuildPattern(TimestampAccuracyRepeatingDecimal, timestampAccuracy=0.9),
        )

        @DataPoints
        @JvmField
        val buildInvalidPattern = arrayOf(
        /* 000 */ BuildInvalidPattern("Directional range error", trackDirection=-1),
        /* 001 */ BuildInvalidPattern("Directional range error", trackDirection=360),
        /* 002 */ BuildInvalidPattern("Latitude range error", latitude=-90.0000001),
        /* 003 */ BuildInvalidPattern("Latitude range error", latitude=90.0000001),
        /* 004 */ BuildInvalidPattern("Longitude range error", longitude=-180.0000001),
        /* 005 */ BuildInvalidPattern("Longitude range error", longitude=180.0000001),
        /* 006 */ BuildInvalidPattern("Altitude range error", pressureAltitude=-999.6),
        /* 007 */ BuildInvalidPattern("Altitude range error", pressureAltitude=31767.1),
        /* 008 */ BuildInvalidPattern("Altitude range error", geodeticAltitude=-999.6),
        /* 009 */ BuildInvalidPattern("Altitude range error", geodeticAltitude=31767.1),
        /* 010 */ BuildInvalidPattern("Altitude range error", height=-999.6),
        /* 011 */ BuildInvalidPattern("Altitude range error", height=31767.1),
        /* 012 */ BuildInvalidPattern("Timestamp range error", timestamp=-1),
        /* 013 */ BuildInvalidPattern("Timestamp range error", timestamp=36001),
        /* 014 */ BuildInvalidPattern("Timestamp Accuracy range error", timestampAccuracy=0.09),
        /* 015 */ BuildInvalidPattern("Timestamp Accuracy range error", timestampAccuracy=1.51),
        )
        // @formatter:on
    }

    @Theory
    fun parse(p: ParsePattern) {
        (OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.LocationVector).let {
            Assert.assertEquals(p.expectedStatus, it.status)
            Assert.assertEquals(p.expectedHeightType, it.heightType)
            Assert.assertEquals(p.expectedDirectionSegment, it.directionSegment)
            Assert.assertEquals(p.expectedSpeedMultiplier, it.speedMultiplier)
            Assert.assertEquals(p.expectedSpeed, it.speed, 0.0)
            Assert.assertEquals(p.expectedVerticalSpeed, it.verticalSpeed, 0.0)
            Assert.assertEquals(p.expectedLatitude, it.latitude, 0.0)
            Assert.assertEquals(p.expectedLongitude, it.longitude, 0.0)
            Assert.assertEquals(p.expectedPressureAltitude, it.pressureAltitude, 0.0)
            Assert.assertEquals(p.expectedGeodeticAltitude, it.geodeticAltitude, 0.0)
            Assert.assertEquals(p.expectedHeight, it.height, 0.0)
            Assert.assertEquals(p.expectedHorizontalAccuracy, it.horizontalAccuracy)
            Assert.assertEquals(p.expectedVerticalAccuracy, it.verticalAccuracy)
            Assert.assertEquals(p.expectedBaroAltitudeAccuracy, it.baroAltitudeAccuracy)
            Assert.assertEquals(p.expectedSpeedAccuracy, it.speedAccuracy)
            Assert.assertEquals(p.expectedTimestamp, it.timestamp100msec)
            Assert.assertEquals(p.expectedTimestampAccuracy, it.timestampAccuracy, 0.0)
        }
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
        try {
            (OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.LocationVector).let {
                it.status
                it.heightType
                it.directionSegment
                it.speedMultiplier
                it.trackDirection
                it.speed
                it.verticalSpeed
                it.latitude
                it.longitude
                it.pressureAltitude
                it.geodeticAltitude
                it.height
                it.horizontalAccuracy
                it.verticalAccuracy
                it.baroAltitudeAccuracy
                it.speedAccuracy
                it.timestamp100msec
                it.timestampAccuracy
                Assert.fail()
            }
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }

    @Theory
    fun build(p: BuildPattern) {
        OpenDroneIDMessage.Message(OpenDroneIDMessage.LocationVector().apply {
            status = p.status
            heightType = p.heightType
            trackDirection = p.trackDirection
            speed = p.speed
            verticalSpeed = p.verticalSpeed
            latitude = p.latitude
            longitude = p.longitude
            pressureAltitude = p.pressureAltitude
            geodeticAltitude = p.geodeticAltitude
            height = p.height
            horizontalAccuracy = p.horizontalAccuracy
            verticalAccuracy = p.verticalAccuracy
            baroAltitudeAccuracy = p.baroAltitudeAccuracy
            speedAccuracy = p.speedAccuracy
            timestamp100msec = p.timestamp
            timestampAccuracy = p.timestampAccuracy
        }).let {
            Assert.assertArrayEquals(p.expectedData, it.toByteArray())
        }
    }

    @Theory
    fun buildInvalid(p: BuildInvalidPattern) {
        try {
            OpenDroneIDMessage.Message(OpenDroneIDMessage.LocationVector().apply {
                status = p.status
                heightType = p.heightType
                trackDirection = p.trackDirection
                speed = p.speed
                verticalSpeed = p.verticalSpeed
                latitude = p.latitude
                longitude = p.longitude
                pressureAltitude = p.pressureAltitude
                geodeticAltitude = p.geodeticAltitude
                height = p.height
                horizontalAccuracy = p.horizontalAccuracy
                verticalAccuracy = p.verticalAccuracy
                baroAltitudeAccuracy = p.baroAltitudeAccuracy
                speedAccuracy = p.speedAccuracy
                timestamp100msec = p.timestamp
                timestampAccuracy = p.timestampAccuracy
                Assert.fail()
            })
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }
}

@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class AuthenticationHeaderTest {
    companion object {
        data class ParsePattern(
            val expectedAuthType: AuthType = AuthType.None,
            val expectedPageNumber: Int = 0,
            val expectedPageCount: Int = 1,
            val expectedLength: Int = 17,
            val expectedUnixTime: Long = 1546300800,
            val expectedAuthData: ByteArray = ByteArray(17) { 0x00 },
            val data: ByteArray,
        )

        data class ParseInvalidPattern(val expectedMsg: String, val data: ByteArray)
        data class BuildPattern(
            val expectedData: ByteArray,
            val authType: AuthType = AuthType.None,
            val pageNumber: Int = 0,
            val pageCount: Int = 1,
            val length: Int = 17,
            val unixTime: Long = 1546300800,
            val authData: ByteArray = ByteArray(17) { 0x00 },
        )

        data class BuildInvalidPattern(
            val expectedMsg: String,
            val authType: AuthType = AuthType.None,
            val pageNumber: Int = 0,
            val pageCount: Int = 1,
            val length: Int = 17,
            val unixTime: Long = 1546300800,
            val authData: ByteArray = ByteArray(17) { 0x00 },
        )

        // @formatter:off
        // AuthType
        val None = byteArrayOf(0x20, 0x00, 0x01, 0x11) + ByteArray(21) {0x00}
        val UASIDSignature = byteArrayOf(0x20, 0x10, 0x01, 0x11) + ByteArray(21) {0x00}
        val OperatorIDSignature = byteArrayOf(0x20, 0x20, 0x01, 0x11) + ByteArray(21) {0x00}
        val MessageSetSignature = byteArrayOf(0x20, 0x30, 0x01, 0x11) + ByteArray(21) {0x00}
        val AuthenticationProvidedbyNetworkRemoteID = byteArrayOf(0x20, 0x40, 0x01, 0x11) + ByteArray(21) {0x00}
        val Reserved1 = byteArrayOf(0x20, 0x50, 0x01, 0x11) + ByteArray(21) {0x00}
        val Reserved2 = byteArrayOf(0x20, 0x60, 0x01, 0x11) + ByteArray(21) {0x00}
        val Reserved3 = byteArrayOf(0x20, 0x70, 0x01, 0x11) + ByteArray(21) {0x00}
        val Reserved4 = byteArrayOf(0x20, 0x80.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val Reserved5 = byteArrayOf(0x20, 0x90.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse1 = byteArrayOf(0x20, 0xA0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse2 = byteArrayOf(0x20, 0xB0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse3 = byteArrayOf(0x20, 0xC0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse4 = byteArrayOf(0x20, 0xD0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse5 = byteArrayOf(0x20, 0xE0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        val PrivateUse6 = byteArrayOf(0x20, 0xF0.toByte(), 0x01, 0x11) + ByteArray(21) {0x00}
        // PageNumber
        val PageNumber0 = byteArrayOf(0x20, 0x00, 0x01, 0x11) + ByteArray(21) {0x00}
        // PageCount
        val PageCount0 = byteArrayOf(0x20, 0x00, 0x00, 0x11) + ByteArray(21) {0x00}
        val PageCount5 = byteArrayOf(0x20, 0x00, 0x05, 0x11) + ByteArray(21) {0x00}
        // Length
        val Length000 = byteArrayOf(0x20, 0x00, 0x01, 0x00) + ByteArray(21) {0x00}
        val Length109 = byteArrayOf(0x20, 0x00, 0x01, 0x6D) + ByteArray(21) {0x00}
        // UnixTime
        val UnixTime1546300800 = byteArrayOf(0x20, 0x00.toByte(), 0x01, 0x11,
            0x00, 0x00, 0x00, 0x00) + ByteArray(17) {0x00}
        val UnixTime3693752040 = byteArrayOf(0x20, 0x00.toByte(), 0x01, 0x11,
            0x68, 0x81.toByte(), 0xFF.toByte(), 0x7F) + ByteArray(17) {0x00}
        // AuthData
        val AuthData = byteArrayOf(0x20, 0x00.toByte(), 0x01, 0x11, 0x00, 0x00, 0x00, 0x00) + ByteArray(17) {0x41}
        // Invalid
        val InvalidDataSizeUnder = byteArrayOf(0x20) + ByteArray(23) {0x00}
        val InvalidDataSizeOver = byteArrayOf(0x20) + ByteArray(25) {0x00}

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
        /* 000 */ ParsePattern(expectedAuthType=AuthType.None, data=None),
        /* 001 */ ParsePattern(expectedAuthType=AuthType.UASIDSignature, data=UASIDSignature),
        /* 002 */ ParsePattern(expectedAuthType=AuthType.OperatorIDSignature, data=OperatorIDSignature),
        /* 003 */ ParsePattern(expectedAuthType=AuthType.MessageSetSignature, data=MessageSetSignature),
        /* 004 */ ParsePattern(expectedAuthType=AuthType.AuthenticationProvidedbyNetworkRemoteID, data=AuthenticationProvidedbyNetworkRemoteID),
        /* 005 */ ParsePattern(expectedAuthType=AuthType.Reserved1, data=Reserved1),
        /* 006 */ ParsePattern(expectedAuthType=AuthType.Reserved2, data=Reserved2),
        /* 007 */ ParsePattern(expectedAuthType=AuthType.Reserved3, data=Reserved3),
        /* 008 */ ParsePattern(expectedAuthType=AuthType.Reserved4, data=Reserved4),
        /* 009 */ ParsePattern(expectedAuthType=AuthType.Reserved5, data=Reserved5),
        /* 010 */ ParsePattern(expectedAuthType=AuthType.PrivateUse1, data=PrivateUse1),
        /* 011 */ ParsePattern(expectedAuthType=AuthType.PrivateUse2, data=PrivateUse2),
        /* 012 */ ParsePattern(expectedAuthType=AuthType.PrivateUse3, data=PrivateUse3),
        /* 013 */ ParsePattern(expectedAuthType=AuthType.PrivateUse4, data=PrivateUse4),
        /* 014 */ ParsePattern(expectedAuthType=AuthType.PrivateUse5, data=PrivateUse5),
        /* 015 */ ParsePattern(expectedAuthType=AuthType.PrivateUse6, data=PrivateUse6),
        /* 016 */ ParsePattern(expectedPageNumber=0, data=PageNumber0),
        /* 017 */ ParsePattern(expectedPageCount=0, data=PageCount0),
        /* 018 */ ParsePattern(expectedPageCount=5, data=PageCount5),
        /* 019 */ ParsePattern(expectedLength=0, data=Length000),
        /* 020 */ ParsePattern(expectedLength=109, data=Length109),
        /* 021 */ ParsePattern(expectedUnixTime=1546300800, data=UnixTime1546300800),
        /* 022 */ ParsePattern(expectedUnixTime=3693752040, data=UnixTime3693752040),
        /* 023 */ ParsePattern(expectedAuthData=ByteArray(17) {0x41}, data=AuthData),
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
        /* 000 */ ParseInvalidPattern("Data length error", InvalidDataSizeUnder),
        /* 001 */ ParseInvalidPattern("Data length error", InvalidDataSizeOver),
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
        /* 000 */ BuildPattern(expectedData=None, authType=AuthType.None),
        /* 001 */ BuildPattern(expectedData=UASIDSignature, authType=AuthType.UASIDSignature),
        /* 002 */ BuildPattern(expectedData=OperatorIDSignature, authType=AuthType.OperatorIDSignature),
        /* 003 */ BuildPattern(expectedData=MessageSetSignature, authType=AuthType.MessageSetSignature),
        /* 004 */ BuildPattern(expectedData=AuthenticationProvidedbyNetworkRemoteID, authType=AuthType.AuthenticationProvidedbyNetworkRemoteID),
        /* 005 */ BuildPattern(expectedData=Reserved1, authType=AuthType.Reserved1),
        /* 006 */ BuildPattern(expectedData=Reserved2, authType=AuthType.Reserved2),
        /* 007 */ BuildPattern(expectedData=Reserved3, authType=AuthType.Reserved3),
        /* 008 */ BuildPattern(expectedData=Reserved4, authType=AuthType.Reserved4),
        /* 009 */ BuildPattern(expectedData=Reserved5, authType=AuthType.Reserved5),
        /* 010 */ BuildPattern(expectedData=PrivateUse1, authType=AuthType.PrivateUse1),
        /* 011 */ BuildPattern(expectedData=PrivateUse2, authType=AuthType.PrivateUse2),
        /* 012 */ BuildPattern(expectedData=PrivateUse3, authType=AuthType.PrivateUse3),
        /* 013 */ BuildPattern(expectedData=PrivateUse4, authType=AuthType.PrivateUse4),
        /* 014 */ BuildPattern(expectedData=PrivateUse5, authType=AuthType.PrivateUse5),
        /* 015 */ BuildPattern(expectedData=PrivateUse6, authType=AuthType.PrivateUse6),
        /* 016 */ BuildPattern(expectedData=PageNumber0, pageNumber=0),
        /* 017 */ BuildPattern(expectedData=PageCount0, pageCount=0),
        /* 018 */ BuildPattern(expectedData=PageCount5, pageCount=5),
        /* 019 */ BuildPattern(expectedData=Length000, length=0),
        /* 020 */ BuildPattern(expectedData=Length109, length=109),
        /* 021 */ BuildPattern(expectedData=UnixTime1546300800, unixTime=1546300800),
        /* 022 */ BuildPattern(expectedData=UnixTime3693752040, unixTime=3693752040),
        /* 023 */ BuildPattern(expectedData=AuthData, authData=ByteArray(17) {0x41}),
        )

        @DataPoints
        @JvmField
        val buildInvalidPattern = arrayOf(
        /* 000 */ BuildInvalidPattern("Page Number range error", pageNumber=-1),
        /* 001 */ BuildInvalidPattern("Page Number range error", pageNumber=1),
        /* 002 */ BuildInvalidPattern("Page Count range error", pageCount=-1),
        /* 003 */ BuildInvalidPattern("Page Count range error", pageCount=6),
        /* 004 */ BuildInvalidPattern("Length range error", length=-1),
        /* 005 */ BuildInvalidPattern("Length range error", length=110),
        /* 006 */ BuildInvalidPattern("UnixTime range error", unixTime=1546300799),
        /* 007 */ BuildInvalidPattern("UnixTime range error", unixTime=3693752041),
        /* 008 */ BuildInvalidPattern("Auth Data length error", authData=ByteArray(12) {0x00}),
        )
        // @formatter:on
    }

    @Theory
    fun parse(p: ParsePattern) {
        (OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.AuthenticationHeader).let {
            Assert.assertEquals(p.expectedAuthType, it.authType)
            Assert.assertEquals(p.expectedPageNumber, it.pageNumber)
            Assert.assertEquals(p.expectedPageCount, it.pageCount)
            Assert.assertEquals(p.expectedLength, it.length)
            Assert.assertEquals(p.expectedUnixTime, it.unixTime)
            Assert.assertArrayEquals(p.expectedAuthData, it.authData)
            Assert.assertEquals(17, it.authDataLength())
        }
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
        try {
            (OpenDroneIDMessage.parse(p.data).payload
                    as OpenDroneIDMessage.AuthenticationHeader).let {
                it.authType
                it.pageNumber
                it.pageCount
                it.length
                it.unixTime
                it.authData
                Assert.fail()
            }
        } catch (e: java.lang.Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }

    @Theory
    fun build(p: BuildPattern) {
        OpenDroneIDMessage.Message(OpenDroneIDMessage.AuthenticationHeader().apply {
            authType = p.authType
            pageNumber = p.pageNumber
            pageCount = p.pageCount
            length = p.length
            unixTime = p.unixTime
            authData = p.authData
        }).let {
            Assert.assertArrayEquals(p.expectedData, it.toByteArray())
        }
    }

    @Theory
    fun buildInvalid(p: BuildInvalidPattern) {
        try {
            OpenDroneIDMessage.Message(OpenDroneIDMessage.AuthenticationHeader().apply {
                authType = p.authType
                pageNumber = p.pageNumber
                pageCount = p.pageCount
                length = p.length
                unixTime = p.unixTime
                authData = p.authData
                Assert.fail()
            })
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }
}

@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class AuthenticationAdditionalTest {
    companion object {
        data class ParsePattern(
            val expectedAuthType: AuthType = AuthType.None,
            val expectedPageNumber: Int = 1,
            val expectedAuthData: ByteArray = ByteArray(23) { 0x00 },
            val data: ByteArray,
        )

        data class ParseInvalidPattern(val expectedMsg: String, val data: ByteArray)
        data class BuildPattern(
            val expectedData: ByteArray,
            val authType: AuthType = AuthType.None,
            val pageNumber: Int = 1,
            val authData: ByteArray = ByteArray(23) { 0x00 },
        )

        data class BuildInvalidPattern(
            val expectedMsg: String,
            val authType: AuthType = AuthType.None,
            val pageNumber: Int = 1,
            val authData: ByteArray = ByteArray(23) { 0x00 },
        )

        // @formatter:off
        // AuthType
        val None = byteArrayOf(0x20, 0x01) + ByteArray(23) {0x00}
        val UASIDSignature = byteArrayOf(0x20, 0x11) + ByteArray(23) {0x00}
        val OperatorIDSignature = byteArrayOf(0x20, 0x21) + ByteArray(23) {0x00}
        val MessageSetSignature = byteArrayOf(0x20, 0x31) + ByteArray(23) {0x00}
        val AuthenticationProvidedbyNetworkRemoteID = byteArrayOf(0x20, 0x41) + ByteArray(23) {0x00}
        val Reserved1 = byteArrayOf(0x20, 0x51) + ByteArray(23) {0x00}
        val Reserved2 = byteArrayOf(0x20, 0x61) + ByteArray(23) {0x00}
        val Reserved3 = byteArrayOf(0x20, 0x71) + ByteArray(23) {0x00}
        val Reserved4 = byteArrayOf(0x20, 0x81.toByte()) + ByteArray(23) {0x00}
        val Reserved5 = byteArrayOf(0x20, 0x91.toByte()) + ByteArray(23) {0x00}
        val PrivateUse1 = byteArrayOf(0x20, 0xA1.toByte()) + ByteArray(23) {0x00}
        val PrivateUse2 = byteArrayOf(0x20, 0xB1.toByte()) + ByteArray(23) {0x00}
        val PrivateUse3 = byteArrayOf(0x20, 0xC1.toByte()) + ByteArray(23) {0x00}
        val PrivateUse4 = byteArrayOf(0x20, 0xD1.toByte()) + ByteArray(23) {0x00}
        val PrivateUse5 = byteArrayOf(0x20, 0xE1.toByte()) + ByteArray(23) {0x00}
        val PrivateUse6 = byteArrayOf(0x20, 0xF1.toByte()) + ByteArray(23) {0x00}
        // PageNumber
        val PageNumber1 = byteArrayOf(0x20, 0x01) + ByteArray(23) {0x00}
        val PageNumber5 = byteArrayOf(0x20, 0x05) + ByteArray(23) {0x00}
        // AuthData
        val AuthData = byteArrayOf(0x20, 0x01) + ByteArray(23) {0x41}
        // Invalid
        val InvalidDataSizeUnder = byteArrayOf(0x20) + ByteArray(23) {0x00}
        val InvalidDataSizeOver = byteArrayOf(0x20) + ByteArray(25) {0x00}

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
        /* 000 */ ParsePattern(expectedAuthType=AuthType.None, data=None),
        /* 001 */ ParsePattern(expectedAuthType=AuthType.UASIDSignature, data=UASIDSignature),
        /* 002 */ ParsePattern(expectedAuthType=AuthType.OperatorIDSignature, data=OperatorIDSignature),
        /* 003 */ ParsePattern(expectedAuthType=AuthType.MessageSetSignature, data=MessageSetSignature),
        /* 004 */ ParsePattern(expectedAuthType=AuthType.AuthenticationProvidedbyNetworkRemoteID, data=AuthenticationProvidedbyNetworkRemoteID),
        /* 005 */ ParsePattern(expectedAuthType=AuthType.Reserved1, data=Reserved1),
        /* 006 */ ParsePattern(expectedAuthType=AuthType.Reserved2, data=Reserved2),
        /* 007 */ ParsePattern(expectedAuthType=AuthType.Reserved3, data=Reserved3),
        /* 008 */ ParsePattern(expectedAuthType=AuthType.Reserved4, data=Reserved4),
        /* 009 */ ParsePattern(expectedAuthType=AuthType.Reserved5, data=Reserved5),
        /* 010 */ ParsePattern(expectedAuthType=AuthType.PrivateUse1, data=PrivateUse1),
        /* 011 */ ParsePattern(expectedAuthType=AuthType.PrivateUse2, data=PrivateUse2),
        /* 012 */ ParsePattern(expectedAuthType=AuthType.PrivateUse3, data=PrivateUse3),
        /* 013 */ ParsePattern(expectedAuthType=AuthType.PrivateUse4, data=PrivateUse4),
        /* 014 */ ParsePattern(expectedAuthType=AuthType.PrivateUse5, data=PrivateUse5),
        /* 015 */ ParsePattern(expectedAuthType=AuthType.PrivateUse6, data=PrivateUse6),
        /* 016 */ ParsePattern(expectedPageNumber=1, data=PageNumber1),
        /* 017 */ ParsePattern(expectedPageNumber=5, data=PageNumber5),
        /* 018 */ ParsePattern(expectedAuthData=ByteArray(23) {0x41}, data=AuthData),
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
        /* 000 */ ParseInvalidPattern("Data length error", InvalidDataSizeUnder),
        /* 001 */ ParseInvalidPattern("Data length error", InvalidDataSizeOver),
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
        /* 000 */ BuildPattern(expectedData=None, authType=AuthType.None),
        /* 001 */ BuildPattern(expectedData=UASIDSignature, authType=AuthType.UASIDSignature),
        /* 002 */ BuildPattern(expectedData=OperatorIDSignature, authType=AuthType.OperatorIDSignature),
        /* 003 */ BuildPattern(expectedData=MessageSetSignature, authType=AuthType.MessageSetSignature),
        /* 004 */ BuildPattern(expectedData=AuthenticationProvidedbyNetworkRemoteID, authType=AuthType.AuthenticationProvidedbyNetworkRemoteID),
        /* 005 */ BuildPattern(expectedData=Reserved1, authType=AuthType.Reserved1),
        /* 006 */ BuildPattern(expectedData=Reserved2, authType=AuthType.Reserved2),
        /* 007 */ BuildPattern(expectedData=Reserved3, authType=AuthType.Reserved3),
        /* 008 */ BuildPattern(expectedData=Reserved4, authType=AuthType.Reserved4),
        /* 009 */ BuildPattern(expectedData=Reserved5, authType=AuthType.Reserved5),
        /* 010 */ BuildPattern(expectedData=PrivateUse1, authType=AuthType.PrivateUse1),
        /* 011 */ BuildPattern(expectedData=PrivateUse2, authType=AuthType.PrivateUse2),
        /* 012 */ BuildPattern(expectedData=PrivateUse3, authType=AuthType.PrivateUse3),
        /* 013 */ BuildPattern(expectedData=PrivateUse4, authType=AuthType.PrivateUse4),
        /* 014 */ BuildPattern(expectedData=PrivateUse5, authType=AuthType.PrivateUse5),
        /* 015 */ BuildPattern(expectedData=PrivateUse6, authType=AuthType.PrivateUse6),
        /* 016 */ BuildPattern(expectedData=PageNumber1, pageNumber=1),
        /* 017 */ BuildPattern(expectedData=PageNumber5, pageNumber=5),
        /* 018 */ BuildPattern(expectedData=AuthData, authData=ByteArray(23) {0x41}),
        )

        @DataPoints
        @JvmField
        val buildInvalidPattern = arrayOf(
        /* 000 */ BuildInvalidPattern("Page Number range error", pageNumber=0),
        /* 001 */ BuildInvalidPattern("Page Number range error", pageNumber=6),
        /* 002 */ BuildInvalidPattern("Auth Data length error", authData=ByteArray(24) {0x00}),
        )
        // @formatter:on
    }

    @Theory
    fun parse(p: ParsePattern) {
        (OpenDroneIDMessage.parse(p.data).payload as OpenDroneIDMessage.AuthenticationAdditional).let {
            Assert.assertEquals(p.expectedAuthType, it.authType)
            Assert.assertEquals(p.expectedPageNumber, it.pageNumber)
            Assert.assertArrayEquals(p.expectedAuthData, it.authData)
            Assert.assertEquals(23, it.authDataLength())
        }
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
        try {
            (OpenDroneIDMessage.parse(p.data).payload
                    as OpenDroneIDMessage.AuthenticationAdditional).let {
                it.authType
                it.pageNumber
                it.authData
                Assert.fail()
            }
        } catch (e: java.lang.Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }

    @Theory
    fun build(p: BuildPattern) {
        OpenDroneIDMessage.Message(OpenDroneIDMessage.AuthenticationAdditional().apply {
            authType = p.authType
            pageNumber = p.pageNumber
            authData = p.authData
        }).let {
            Assert.assertArrayEquals(p.expectedData, it.toByteArray())
        }
    }

    @Theory
    fun buildInvalid(p: BuildInvalidPattern) {
        try {
            OpenDroneIDMessage.Message(OpenDroneIDMessage.AuthenticationAdditional().apply {
                authType = p.authType
                pageNumber = p.pageNumber
                authData = p.authData
                Assert.fail()
            })
        } catch (e: Exception) {
            Assert.assertEquals(p.expectedMsg, e.message)
        }
    }
}
/*
@ExperimentalUnsignedTypes
@RunWith(Theories::class)
class TemplateTest {
    companion object {
        data class ParsePattern(val expected: Int,  val data: ByteArray)
        data class ParseInvalidPattern(val expectedMsg: String, val data: ByteArray)
        data class BuildPattern(val expectedData: ByteArray, val input: Int)
        data class BuildInvalidPattern(val expectedMsg: String, val input: Int)

        // @formatter:off
        val None = byteArrayOf(
            0x00,                                                        // header
            0x00,                                                        // IDType/UAType
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // UAS ID
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00                                             // Reserved
        )

        @DataPoints
        @JvmField
        val parsePattern = arrayOf(
            ParsePattern(0, byteArrayOf(0x00))
        )

        @DataPoints
        @JvmField
        val parseInvalidPattern = arrayOf(
            ParseInvalidPattern("", byteArrayOf(0x00))
        )

        @DataPoints
        @JvmField
        val buildPattern = arrayOf(
            BuildPattern(byteArrayOf(0x00), 0),
        )

        @DataPoints
        @JvmField
        val buildInvalidPattern = arrayOf(
            BuildInvalidPattern("", 0)
        )
        // @formatter:on
    }

    @Theory
    fun parse(p: ParsePattern) {
    }

    @Theory
    fun parseInvalid(p: ParseInvalidPattern) {
    }

    @Theory
    fun build(p: BuildPattern) {
    }

    @Theory
    fun buildInvalid(p: BuildInvalidPattern) {
    }
}
*/

@ExperimentalUnsignedTypes
class OpenDroneIDMessageTest {
    val authentication0_min = byteArrayOf(
        0x20.toByte(),                                              // header
        0x00,                                                       // AuthType/PageNumber
        0x01,                                                       // PageCount
        0x11,                                                       // Length
        0x00, 0x00, 0x00, 0x00,                                     // Timestamp
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // AuthData
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    )

    val authentication0_max = byteArrayOf(
        0x20.toByte(),                                              // header
        0x40,                                                       // AuthType/PageNumber
        0x05,                                                       // PageCount
        0x6D,                                                       // Length
        0x68.toByte(), 0x81.toByte(), 0xFF.toByte(), 0x7F.toByte(), // Timestamp
        0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, // AuthData
        0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10
    )

    @Test
    fun authentication_auth0_parse() {
        val min = OpenDroneIDMessage.parse(authentication0_min).payload as
                OpenDroneIDMessage.AuthenticationHeader
        Assert.assertEquals(OpenDroneIDMessage.AuthType.None, min.authType)
        Assert.assertEquals(0, min.pageNumber)
        Assert.assertEquals(1, min.pageCount)
        Assert.assertEquals(17, min.length)
        Assert.assertEquals(1546300800, min.unixTime)
        Assert.assertArrayEquals(ByteArray(17) { 0x00 }, min.authData)

        val max = OpenDroneIDMessage.parse(authentication0_max).payload as
                OpenDroneIDMessage.AuthenticationHeader
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            max.authType
        )
        Assert.assertEquals(0, max.pageNumber)
        Assert.assertEquals(5, max.pageCount)
        Assert.assertEquals(109, max.length)
        Assert.assertEquals(3693752040, max.unixTime)
        Assert.assertArrayEquals(ByteArray(17) { 0x10 }, max.authData)
    }

    @Test
    fun authentication_auth0_build() {
        val min = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationHeader().apply {
                authType = OpenDroneIDMessage.AuthType.None
                pageNumber = 0
                pageCount = 1
                length = 17
                unixTime = 1546300800
                authData = ByteArray(17) { 0x00 }
            })
        Assert.assertArrayEquals(authentication0_min, min.toByteArray())

        val max = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationHeader().apply {
                authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                pageNumber = 0
                pageCount = 5
                length = 109
                unixTime = 3693752040
                authData = ByteArray(17) { 0x10 }
            })
        Assert.assertArrayEquals(authentication0_max, max.toByteArray())
    }

    val authentication1_max = byteArrayOf(
        0x20.toByte(),                                              // header
        0x41,                                                       // AuthType/PageNumber
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, // AuthData
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
        0x01, 0x01, 0x01
    )

    val authentication2_max = byteArrayOf(
        0x20.toByte(),                                              // header
        0x42,                                                       // AuthType/PageNumber
        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, // AuthData
        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
        0x02, 0x02, 0x02
    )

    val authentication3_max = byteArrayOf(
        0x20.toByte(),                                              // header
        0x43,                                                       // AuthType/PageNumber
        0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, // AuthData
        0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
        0x03, 0x03, 0x03
    )

    val authentication4_max = byteArrayOf(
        0x20.toByte(),                                              // header
        0x44,                                                       // AuthType/PageNumber
        0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, // AuthData
        0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
        0x04, 0x04, 0x04
    )

    @Test
    fun authentication_auth1_parse() {
        val auth1 = OpenDroneIDMessage.parse(authentication1_max).payload as
                OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            auth1.authType
        )
        Assert.assertEquals(1, auth1.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x01 }, auth1.authData)

        val auth2 = OpenDroneIDMessage.parse(authentication2_max).payload as
                OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            auth2.authType
        )
        Assert.assertEquals(2, auth2.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x02 }, auth2.authData)

        val auth3 = OpenDroneIDMessage.parse(authentication3_max).payload as
                OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            auth3.authType
        )
        Assert.assertEquals(3, auth3.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x03 }, auth3.authData)

        val auth4 = OpenDroneIDMessage.parse(authentication4_max).payload as
                OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            auth4.authType
        )
        Assert.assertEquals(4, auth4.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x04 }, auth4.authData)
    }

    @Test
    fun authentication_auth1_build() {
        val auth1 = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationAdditional().apply {
                authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                pageNumber = 1
                authData = ByteArray(23) { 0x01 }
            })
        Assert.assertArrayEquals(authentication1_max, auth1.toByteArray())

        val auth2 = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationAdditional().apply {
                authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                pageNumber = 2
                authData = ByteArray(23) { 0x02 }
            })
        Assert.assertArrayEquals(authentication2_max, auth2.toByteArray())

        val auth3 = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationAdditional().apply {
                authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                pageNumber = 3
                authData = ByteArray(23) { 0x03 }
            })
        Assert.assertArrayEquals(authentication3_max, auth3.toByteArray())

        val auth4 = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.AuthenticationAdditional().apply {
                authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                pageNumber = 4
                authData = ByteArray(23) { 0x04 }
            })
        Assert.assertArrayEquals(authentication4_max, auth4.toByteArray())
    }

    val selfID_min = byteArrayOf(
        0x30.toByte(),                                              // header
        0x00,                                                       // DescriptionType
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Description
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00
    )
    val selfID_max = byteArrayOf(
        0x30.toByte(),                                              // header
        0xFF.toByte(),                                              // DescriptionType
        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, // Description
        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
        0x41, 0x41, 0x41
    )

    @Test
    fun selfID_parse() {
        val min = OpenDroneIDMessage.parse(selfID_min).payload as OpenDroneIDMessage.SelfID
        Assert.assertEquals(0, min.descriptionType)
        Assert.assertEquals("", min.description)

        val max = OpenDroneIDMessage.parse(selfID_max).payload as OpenDroneIDMessage.SelfID
        Assert.assertEquals(255, max.descriptionType)
        Assert.assertEquals("AAAAAAAAAAAAAAAAAAAAAAA", max.description)
    }

    @Test
    fun selfID_build() {
        val min = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.SelfID().apply {
                descriptionType = 0
                description = ""
            }
        )
        Assert.assertArrayEquals(selfID_min, min.toByteArray())

        val max = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.SelfID().apply {
                descriptionType = 255
                description = "AAAAAAAAAAAAAAAAAAAAAAA"
            }
        )
        Assert.assertArrayEquals(selfID_max, max.toByteArray())
    }

    val system_min = byteArrayOf(
        0x40.toByte(),                                 // header
        0x00.toByte(),                                 // OperatorLocationtype
        0x00, 0x17, 0x5B, 0xCA.toByte(),               // OperatorLatitude
        0x00, 0x2E, 0xB6.toByte(), 0x94.toByte(),      // OperatorLongitude
        0x01, 0x00,                                    // AreaCount
        0x00,                                          // AreaRadius
        0x00, 0x00,                                    // AreaCeiling
        0x00, 0x00,                                    // AreaFloor
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // Reserved
    )
    val system_max = byteArrayOf(
        0x40.toByte(),                                 // header
        0x02.toByte(),                                 // OperatorLocationtype
        0x00, 0xE9.toByte(), 0xA4.toByte(), 0x35,      // OperatorLatitude
        0x00, 0xD2.toByte(), 0x49, 0x6B.toByte(),      // OperatorLongitude
        0xE8.toByte(), 0xFD.toByte(),                  // AreaCount
        0x19,                                          // AreaRadius
        0xFE.toByte(), 0xFF.toByte(),                  // AreaCeiling
        0xFE.toByte(), 0xFF.toByte(),                  // AreaFloor
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // Reserved
    )

    @Test
    fun system_parse() {
        val min = OpenDroneIDMessage.parse(system_min).payload as OpenDroneIDMessage.System
        Assert.assertEquals(
            OpenDroneIDMessage.OperatorLocationType.TakeOff,
            min.operatorLocationtype
        )
        Assert.assertEquals(-90.0, min.operatorLatitude, 0.0)
        Assert.assertEquals(-180.0, min.operatorLogitude, 0.0)
        Assert.assertEquals(1, min.areaCount)
        Assert.assertEquals(0.0, min.areaRadius, 0.0)
        Assert.assertEquals(-1000.0, min.areaCeiling, 0.0)
        Assert.assertEquals(-1000.0, min.areaFloor, 0.0)

        val max = OpenDroneIDMessage.parse(system_max).payload as OpenDroneIDMessage.System
        Assert.assertEquals(
            OpenDroneIDMessage.OperatorLocationType.FixedLocation,
            max.operatorLocationtype
        )
        Assert.assertEquals(90.0, max.operatorLatitude, 0.0)
        Assert.assertEquals(180.0, max.operatorLogitude, 0.0)
        Assert.assertEquals(65000, max.areaCount)
        Assert.assertEquals(2.5, max.areaRadius, 0.0)
        Assert.assertEquals(31767.0, max.areaCeiling, 0.0)
        Assert.assertEquals(31767.0, max.areaFloor, 0.0)
    }

    @Test
    fun system_build() {
        val min = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.System().apply {
                operatorLocationtype = OpenDroneIDMessage.OperatorLocationType.TakeOff
                operatorLatitude = -90.0
                operatorLogitude = -180.0
                areaCount = 1
                areaRadius = 0.0
                areaCeiling = -1000.0
                areaFloor = -1000.0
            }
        )
        Assert.assertArrayEquals(system_min, min.toByteArray())

        val max = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.System().apply {
                operatorLocationtype = OpenDroneIDMessage.OperatorLocationType.FixedLocation
                operatorLatitude = 90.0
                operatorLogitude = 180.0
                areaCount = 65000
                areaRadius = 2.5
                areaCeiling = 31767.0
                areaFloor = 31767.0
            }
        )
        Assert.assertArrayEquals(system_max, max.toByteArray())
    }

    val operatorID_min = byteArrayOf(
        0x50.toByte(),                                              // header
        0x00,                                                       // OperatorIDType
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // OperatorID
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00                                            // Reserved
    )

    val operatorID_max = byteArrayOf(
        0x50.toByte(),                                              // header
        0xFF.toByte(),                                              // OperatorIDType
        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, // OperatorID
        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
        0x00, 0x00, 0x00                                            // Reserved
    )

    @Test
    fun operatorID_parse() {
        val min = OpenDroneIDMessage.parse(operatorID_min).payload as OpenDroneIDMessage.OperatorID
        Assert.assertEquals(0, min.operatorIDType)
        Assert.assertEquals("", min.operatorID)

        val max = OpenDroneIDMessage.parse(operatorID_max).payload as OpenDroneIDMessage.OperatorID
        Assert.assertEquals(255, max.operatorIDType)
        Assert.assertEquals("AAAAAAAAAAAAAAAAAAAA", max.operatorID)
    }

    @Test
    fun operatorID_build() {
        val min = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.OperatorID().apply {
                operatorIDType = 0
                operatorID = ""
            }
        )
        Assert.assertArrayEquals(operatorID_min, min.toByteArray())

        val max = OpenDroneIDMessage.Message(
            OpenDroneIDMessage.OperatorID().apply {
                operatorIDType = 255
                operatorID = "AAAAAAAAAAAAAAAAAAAA"
            }
        )
        Assert.assertArrayEquals(operatorID_max, max.toByteArray())
    }

    val messagePack_min = byteArrayOf(
        0xF0.toByte(), // header
        0x19,          // MessageSize
        0x00           // No of Msgs in Pack
    )

    val messagePack_max = byteArrayOf(
        0xF0.toByte(), // header
        0x19,          // MessageSize
        0x0A           // No of Msgs in Pack
    ) + BasicIDTest.None + LocationVectorTest.Undeclared + authentication0_max +
            authentication1_max + authentication2_max + authentication3_max + authentication4_max +
            selfID_min + system_min + operatorID_min

    @Test
    fun messagePack_parse() {
        val min =
            OpenDroneIDMessage.parse(messagePack_min).payload as OpenDroneIDMessage.MessagePack
        Assert.assertEquals(25, min.messageSize)
        Assert.assertEquals(0, min.messageNumber)
        Assert.assertEquals(0, min.messages.size)

        val max =
            OpenDroneIDMessage.parse(messagePack_max).payload as OpenDroneIDMessage.MessagePack
        Assert.assertEquals(25, max.messageSize)
        Assert.assertEquals(10, max.messageNumber)
        Assert.assertEquals(10, max.messages.size)
        Assert.assertTrue(max.messages[0].payload is OpenDroneIDMessage.BasicID)
        Assert.assertTrue(max.messages[1].payload is OpenDroneIDMessage.LocationVector)
        Assert.assertTrue(max.messages[2].payload is OpenDroneIDMessage.AuthenticationHeader)
        Assert.assertTrue(max.messages[3].payload is OpenDroneIDMessage.AuthenticationAdditional)
        Assert.assertTrue(max.messages[4].payload is OpenDroneIDMessage.AuthenticationAdditional)
        Assert.assertTrue(max.messages[5].payload is OpenDroneIDMessage.AuthenticationAdditional)
        Assert.assertTrue(max.messages[6].payload is OpenDroneIDMessage.AuthenticationAdditional)
        Assert.assertTrue(max.messages[7].payload is OpenDroneIDMessage.SelfID)
        Assert.assertTrue(max.messages[8].payload is OpenDroneIDMessage.System)
        Assert.assertTrue(max.messages[9].payload is OpenDroneIDMessage.OperatorID)
    }

    @Test
    fun messagePack_build() {
        val min = OpenDroneIDMessage.Message(OpenDroneIDMessage.MessagePack())
        Assert.assertArrayEquals(messagePack_min, min.toByteArray())

        val max = OpenDroneIDMessage.Message(OpenDroneIDMessage.MessagePack().apply {
            messages = arrayOf(
                OpenDroneIDMessage.Message(OpenDroneIDMessage.BasicID().apply {
                    uaType = UAType.None
                    uasID = OpenDroneIDMessage.NoneID()
                }),
                OpenDroneIDMessage.Message(OpenDroneIDMessage.LocationVector().apply {
                    status = OpenDroneIDMessage.OperationalStatus.Undeclared
                    heightType = OpenDroneIDMessage.HeightType.AboveTakeoff
                    trackDirection = 0
                    speed = 0.0
                    verticalSpeed = -63.5
                    latitude = -90.0
                    longitude = -180.0
                    pressureAltitude = -1000.0
                    geodeticAltitude = -1000.0
                    height = -1000.0
                    horizontalAccuracy = OpenDroneIDMessage.HorizontalAccuracy.Unknown
                    verticalAccuracy = OpenDroneIDMessage.VerticalAccuracy.Unknown
                    baroAltitudeAccuracy = OpenDroneIDMessage.VerticalAccuracy.Unknown
                    speedAccuracy = OpenDroneIDMessage.SpeedAccuracy.Unknown
                    timestamp100msec = 0
                    timestampAccuracy = 0.1
                })
            ) + OpenDroneIDMessage.AuthBuilder().apply {
                authType =
                    OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
                unixTime = 3693752040
                authData =
                    authentication0_max.sliceArray(8 until authentication0_min.size) +
                            authentication1_max.sliceArray(2 until authentication1_max.size) +
                            authentication2_max.sliceArray(2 until authentication2_max.size) +
                            authentication3_max.sliceArray(2 until authentication3_max.size) +
                            authentication4_max.sliceArray(2 until authentication4_max.size)
            }.messages + arrayOf(
                OpenDroneIDMessage.Message(OpenDroneIDMessage.SelfID().apply {
                    descriptionType = 0
                    description = ""
                }),
                OpenDroneIDMessage.Message(OpenDroneIDMessage.System().apply {
                    operatorLocationtype = OpenDroneIDMessage.OperatorLocationType.TakeOff
                    operatorLatitude = -90.0
                    operatorLogitude = -180.0
                    areaCount = 1
                    areaRadius = 0.0
                    areaCeiling = -1000.0
                    areaFloor = -1000.0
                }),
                OpenDroneIDMessage.Message(OpenDroneIDMessage.OperatorID().apply {
                    operatorIDType = 0
                    operatorID = ""
                })
            )
        })
        Assert.assertArrayEquals(messagePack_max, max.toByteArray())
    }

    @Test
    fun auth_builder_parse() {
        val minBuilder = OpenDroneIDMessage.AuthBuilder().apply {
            add(OpenDroneIDMessage.parse(authentication0_min).payload as OpenDroneIDMessage.Authentication)
        }
        Assert.assertEquals(OpenDroneIDMessage.AuthType.None, minBuilder.authType)
        Assert.assertEquals(1546300800, minBuilder.unixTime)
        Assert.assertArrayEquals(ByteArray(17) { 0x00 }, minBuilder.authData)

        val maxBuilder = OpenDroneIDMessage.AuthBuilder().apply {
            add(OpenDroneIDMessage.parse(authentication0_max).payload as OpenDroneIDMessage.Authentication)
            add(OpenDroneIDMessage.parse(authentication1_max).payload as OpenDroneIDMessage.Authentication)
            add(OpenDroneIDMessage.parse(authentication2_max).payload as OpenDroneIDMessage.Authentication)
            add(OpenDroneIDMessage.parse(authentication3_max).payload as OpenDroneIDMessage.Authentication)
            add(OpenDroneIDMessage.parse(authentication4_max).payload as OpenDroneIDMessage.Authentication)
        }
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxBuilder.authType
        )
        Assert.assertEquals(3693752040, maxBuilder.unixTime)
        Assert.assertArrayEquals(ByteArray(109) { i ->
            when (i) {
                in 0..16 -> 0x10
                in 17..39 -> 0x01
                in 40..62 -> 0x02
                in 63..85 -> 0x03
                in 86..108 -> 0x04
                else -> 0x00
            }
        }, maxBuilder.authData)
    }

    @Test
    fun auth_builder_build() {
        val minBuilder = OpenDroneIDMessage.AuthBuilder().apply {
            authType = OpenDroneIDMessage.AuthType.None
            unixTime = 1546300800
            authData = authentication0_min.sliceArray(8 until authentication0_min.size)
        }
        Assert.assertEquals(1, minBuilder.messages.size)
        val minAuth0 =
            minBuilder.messages[0].payload as OpenDroneIDMessage.AuthenticationHeader
        Assert.assertEquals(OpenDroneIDMessage.AuthType.None, minAuth0.authType)
        Assert.assertEquals(0, minAuth0.pageNumber)
        Assert.assertEquals(1, minAuth0.pageCount)
        Assert.assertEquals(17, minAuth0.length)
        Assert.assertEquals(1546300800, minAuth0.unixTime)
        Assert.assertArrayEquals(ByteArray(17) { 0x00 }, minAuth0.authData)

        val maxBuilder = OpenDroneIDMessage.AuthBuilder().apply {
            authType = OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID
            unixTime = 3693752040
            authData = authentication0_max.sliceArray(8 until authentication0_min.size) +
                    authentication1_max.sliceArray(2 until authentication1_max.size) +
                    authentication2_max.sliceArray(2 until authentication2_max.size) +
                    authentication3_max.sliceArray(2 until authentication3_max.size) +
                    authentication4_max.sliceArray(2 until authentication4_max.size)
        }
        Assert.assertEquals(5, maxBuilder.messages.size)
        val maxAuth0 =
            maxBuilder.messages[0].payload as OpenDroneIDMessage.AuthenticationHeader
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxAuth0.authType
        )
        Assert.assertEquals(0, maxAuth0.pageNumber)
        Assert.assertEquals(5, maxAuth0.pageCount)
        Assert.assertEquals(109, maxAuth0.length)
        Assert.assertEquals(3693752040, maxAuth0.unixTime)
        Assert.assertArrayEquals(ByteArray(17) { 0x10 }, maxAuth0.authData)
        val maxAuth1 =
            maxBuilder.messages[1].payload as OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxAuth1.authType
        )
        Assert.assertEquals(1, maxAuth1.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x01 }, maxAuth1.authData)
        val maxAuth2 =
            maxBuilder.messages[2].payload as OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxAuth2.authType
        )
        Assert.assertEquals(2, maxAuth2.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x02 }, maxAuth2.authData)
        val maxAuth3 =
            maxBuilder.messages[3].payload as OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxAuth3.authType
        )
        Assert.assertEquals(3, maxAuth3.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x03 }, maxAuth3.authData)
        val maxAuth4 =
            maxBuilder.messages[4].payload as OpenDroneIDMessage.AuthenticationAdditional
        Assert.assertEquals(
            OpenDroneIDMessage.AuthType.AuthenticationProvidedbyNetworkRemoteID,
            maxAuth4.authType
        )
        Assert.assertEquals(4, maxAuth4.pageNumber)
        Assert.assertArrayEquals(ByteArray(23) { 0x04 }, maxAuth4.authData)
    }
}
