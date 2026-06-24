package readserial

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ReadSerialTester extends AnyFlatSpec with ChiselScalatestTester {

  "ReadSerial" should "work" in {

    test(new ReadSerial)
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // Release reset

      dut.io.reset_n.poke(true.B)

      // Idle bus

      dut.io.rxd.poke(true.B)

      dut.clock.step(2)

      dut.io.valid.expect(false.B)

      // =========================================
      // TEST CASE 1
      // 10101010
      // =========================================

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      val data1 = Seq(
        true.B,
        false.B,
        true.B,
        false.B,
        true.B,
        false.B,
        true.B,
        false.B
      )

      for(bit <- data1){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b10101010".U)

      dut.clock.step(1)

      // =========================================
      // TEST CASE 2
      // RESET DURING TRANSMISSION
      // =========================================

      dut.io.rxd.poke(false.B)   // start bit
      dut.clock.step(1)

      dut.io.rxd.poke(true.B)    // bit7
      dut.clock.step(1)

      dut.io.rxd.poke(false.B)   // bit6
      dut.clock.step(1)

      dut.io.rxd.poke(true.B)    // bit5
      dut.clock.step(1)

      // Assert reset in the middle of reception

      dut.io.reset_n.poke(true.B)

      dut.clock.step(1)

      // Receiver should be reset

      dut.io.valid.expect(false.B)

      dut.io.data.expect(0.U)

      // Release reset

      dut.io.reset_n.poke(true.B)

      dut.clock.step(1)

      // Start a NEW transmission

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      val resetData = Seq(
        true.B,
        true.B,
        true.B,
        true.B,
        false.B,
        false.B,
        false.B,
        false.B
      )

      for(bit <- resetData){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11110000".U)

            // =========================================
      // TEST CASE 3
      // Back-to-back transmission
      // 11001100 -> 00110011
      // =========================================

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      val packet1 = Seq(
        true.B, true.B,
        false.B, false.B,
        true.B, true.B,
        false.B, false.B
      )

      for(bit <- packet1){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11001100".U)

      dut.clock.step(1)

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      val packet2 = Seq(
        false.B, false.B,
        true.B, true.B,
        false.B, false.B,
        true.B, true.B
      )

      for(bit <- packet2){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b00110011".U)

      dut.clock.step(1)

      // =========================================
      // TEST CASE 4
      // Three consecutive packets
      // =========================================

      val packetA = Seq(
        true.B,true.B,true.B,true.B,
        false.B,false.B,false.B,false.B
      )

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(bit <- packetA){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11110000".U)

      dut.clock.step(1)

      val packetB = Seq(
        true.B,false.B,true.B,false.B,
        true.B,false.B,true.B,false.B
      )

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(bit <- packetB){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b10101010".U)

      dut.clock.step(1)

      val packetC = Seq(
        false.B,false.B,false.B,false.B,
        true.B,true.B,true.B,true.B
      )

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(bit <- packetC){

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b00001111".U)

      dut.clock.step(1)

      // =========================================
      // TEST CASE 5
      // 11111111 -> 00000000
      // =========================================

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(i <- 0 until 8){

        dut.io.rxd.poke(true.B)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11111111".U)

      dut.clock.step(1)

      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(i <- 0 until 8){

        dut.io.rxd.poke(false.B)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b00000000".U)

      dut.clock.step(5)

      }
  }
}
