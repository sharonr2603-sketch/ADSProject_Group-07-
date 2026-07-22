package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import PipelinedRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class PipelinedRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

  "RV32I_BTBTester" should "execute correctly" in {

    test(new PipelinedRV32I("src/test/programs/BinaryFile_pipelined"))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)

      dut.clock.step(5)

    
      waitForResult(dut, 5)
      waitForResult(dut, 5)


      waitForResult(dut, 101)

      
      waitForResult(dut, 100)

     
      waitForResult(dut, 55)

     
      waitForResult(dut, 4)

     
      dut.clock.step(20)

      dut.io.exception.expect(false.B)
    }
  }

  def waitForResult(
      dut: PipelinedRV32I,
      value: BigInt,
      maxCycles: Int = 40
  ): Unit = {

    var found = false
    var cycles = 0

    while (!found && cycles < maxCycles) {

      if (dut.io.result.peek().litValue == value) {
        dut.io.result.expect(value.U)
        found = true
      } else {
        dut.clock.step()
        cycles += 1
      }
    }

    assert(found, s"Result $value not observed within $maxCycles cycles")
  }

}

