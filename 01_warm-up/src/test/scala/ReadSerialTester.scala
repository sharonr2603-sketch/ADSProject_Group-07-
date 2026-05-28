// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package readserial

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/** 
  * Read Serial Tester
  */
class ReadSerialTester extends AnyFlatSpec with ChiselScalatestTester {

  "ReadSerial" should "work" in {

    test(new ReadSerial)
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      
      // Initial idle condition
      // Bus idle = 1
      
      dut.io.rxd.poke(true.B)

      dut.clock.step(2)

      dut.io.valid.expect(false.B)

      
      // TEST CASE 1
      // Receive: 10101010
      // MSB first
      

      // Start bit
      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      // Data bits
      dut.io.rxd.poke(true.B)   // bit7
      dut.clock.step(1)

      dut.io.rxd.poke(false.B)  // bit6
      dut.clock.step(1)

      dut.io.rxd.poke(true.B)   // bit5
      dut.clock.step(1)

      dut.io.rxd.poke(false.B)  // bit4
      dut.clock.step(1)

      dut.io.rxd.poke(true.B)   // bit3
      dut.clock.step(1)

      dut.io.rxd.poke(false.B)  // bit2
      dut.clock.step(1)

      dut.io.rxd.poke(true.B)   // bit1
      dut.clock.step(1)

      dut.io.rxd.poke(false.B)  // bit0
      dut.clock.step(1)

      // Check output
      //dut.clock.step(1)

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b10101010".U)

      
      // TEST CASE 2
      // Receive: 11111111
      // All ones
      

      // Start bit
      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(i <- 0 until 8) {

        dut.io.rxd.poke(true.B)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11111111".U)


      
      // TEST CASE 3
      // Receive: 00000000
      // All zeros
      

      // Start bit
      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      for(i <- 0 until 8) {

        dut.io.rxd.poke(false.B)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b00000000".U)

      
      // TEST CASE 4
      // Back-to-back transmission
      // No idle cycle in between
      

      // Start bit of first transmission
      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      // 11001100
      val data1 = Seq(
        true.B, true.B,
        false.B, false.B,
        true.B, true.B,
        false.B, false.B
      )

      for(bit <- data1) {

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b11001100".U)

      // Immediately next transmission
      // Start bit again
      dut.io.rxd.poke(false.B)
      dut.clock.step(1)

      // 00110011
      val data2 = Seq(
        false.B, false.B,
        true.B, true.B,
        false.B, false.B,
        true.B, true.B
      )

      for(bit <- data2) {

        dut.io.rxd.poke(bit)

        dut.clock.step(1)
      }

      dut.io.valid.expect(true.B)

      dut.io.data.expect("b00110011".U)

      dut.clock.step(5)


      
    }
  }
}