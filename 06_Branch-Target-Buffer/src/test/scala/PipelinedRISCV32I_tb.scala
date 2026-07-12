// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)


package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import PipelinedRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class PipelinedRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

  "RV32I_BTBTester" should "learn branch target" in {

    test(new PipelinedRV32I("src/test/programs/BinaryFile_pipelined"))
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)

      dut.clock.step(5)

      dut.io.result.expect(5.U)
      dut.clock.step()

      dut.io.result.expect(5.U)
      dut.clock.step()

      dut.io.result.expect(0.U)
      dut.clock.step()

      dut.io.result.expect(0.U)
      dut.clock.step()

      dut.io.result.expect(0.U)
      dut.clock.step()

      dut.io.result.expect(101.U)
      dut.clock.step()

      dut.io.result.expect(0.U)
      dut.clock.step()

      var found = false

      while (!found) {
        if (dut.io.result.peek().litValue == 101) {
          found = true
        } else {
          dut.clock.step()
        }
      }

      dut.io.result.expect(101.U)
      dut.io.exception.expect(false.B)
    

    }
  }


}