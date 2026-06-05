// ADS I Class Project
// Assignment 02 - ALU Testbench

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import Assignment02._

// Test ADD operation
class ALUAddTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Add_Tester" should "test ADD operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal addition
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(AluOp.ADD)
      dut.io.aluResult.expect(20.U)
      dut.clock.step(1)

      // Add zero
      dut.io.operandA.poke(123.U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(AluOp.ADD)
      dut.io.aluResult.expect(123.U)
      dut.clock.step(1)

      // Overflow wraparound
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.ADD)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)
    }
  }
}

// Test SUB operation
class ALUSubTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sub_Tester" should "test SUB operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal subtraction
      dut.io.operandA.poke(20.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(AluOp.SUB)
      dut.io.aluResult.expect(10.U)
      dut.clock.step(1)

      // Subtract zero
      dut.io.operandA.poke(99.U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(AluOp.SUB)
      dut.io.aluResult.expect(99.U)
      dut.clock.step(1)

      // Underflow wraparound
      dut.io.operandA.poke(0.U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.SUB)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)
    }
  }
}

// Test AND operation
class ALUAndTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_And_Tester" should "test AND operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal AND
      dut.io.operandA.poke("hFF00FF00".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(AluOp.AND)
      dut.io.aluResult.expect("h0F000F00".U)
      dut.clock.step(1)

      // AND with zero
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(AluOp.AND)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // AND with all ones
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(AluOp.AND)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)
    }
  }
}

// Test OR operation
class ALUOrTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Or_Tester" should "test OR operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal OR
      dut.io.operandA.poke("hFF00FF00".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(AluOp.OR)
      dut.io.aluResult.expect("hFF0FFF0F".U)
      dut.clock.step(1)

      // OR with zero
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(AluOp.OR)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // OR with all ones
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(AluOp.OR)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)
    }
  }
}

// Test XOR operation
class ALUXorTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Xor_Tester" should "test XOR operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal XOR
      dut.io.operandA.poke("hF0F0F0F0".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(AluOp.XOR)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // XOR same values
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(AluOp.XOR)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // XOR with zero
      dut.io.operandA.poke("hABCDEF12".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(AluOp.XOR)
      dut.io.aluResult.expect("hABCDEF12".U)
      dut.clock.step(1)
    }
  }
}

// Test SLL operation
class ALUSllTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sll_Tester" should "test SLL operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Normal shift left
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(AluOp.SLL)
      dut.io.aluResult.expect(16.U)
      dut.clock.step(1)

      // Shift by 31
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke(31.U)
      dut.io.operation.poke(AluOp.SLL)
      dut.io.aluResult.expect("h80000000".U)
      dut.clock.step(1)

      // Only lower 5 bits of operandB are used: 36 -> 4
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke(36.U)
      dut.io.operation.poke(AluOp.SLL)
      dut.io.aluResult.expect(16.U)
      dut.clock.step(1)
    }
  }
}

// Test SRL operation
class ALUSrlTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Srl_Tester" should "test SRL operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Logical right shift
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.SRL)
      dut.io.aluResult.expect("h40000000".U)
      dut.clock.step(1)

      // Shift by 31
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(31.U)
      dut.io.operation.poke(AluOp.SRL)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Only lower 5 bits of operandB are used: 36 -> 4
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(36.U)
      dut.io.operation.poke(AluOp.SRL)
      dut.io.aluResult.expect("h08000000".U)
      dut.clock.step(1)
    }
  }
}

// Test SRA operation
class ALUSraTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sra_Tester" should "test SRA operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Arithmetic right shift of negative number
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.SRA)
      dut.io.aluResult.expect("hC0000000".U)
      dut.clock.step(1)

      // Arithmetic right shift keeps sign bit
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(AluOp.SRA)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // Positive number arithmetic shift behaves like logical shift
      dut.io.operandA.poke("h00000080".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(AluOp.SRA)
      dut.io.aluResult.expect("h00000008".U)
      dut.clock.step(1)
    }
  }
}

// Test SLT operation
class ALUSltTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Slt_Tester" should "test SLT operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Signed: -1 < 1
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.SLT)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Signed: 1 < -1 is false
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(AluOp.SLT)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Equal values
      dut.io.operandA.poke(5.U)
      dut.io.operandB.poke(5.U)
      dut.io.operation.poke(AluOp.SLT)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)
    }
  }
}

// Test SLTU operation
class ALUSltuTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sltu_Tester" should "test SLTU operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Unsigned: 1 < 2
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke(2.U)
      dut.io.operation.poke(AluOp.SLTU)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Unsigned: 0xFFFFFFFF > 1
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(AluOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Equal values
      dut.io.operandA.poke("hABCDEF12".U)
      dut.io.operandB.poke("hABCDEF12".U)
      dut.io.operation.poke(AluOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)
    }
  }
}

// Test PASSB operation
class ALUPassBTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_PassB_Tester" should "test PASSB operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Pass operandB
      dut.io.operandA.poke(0.U)
      dut.io.operandB.poke("hDEADBEEF".U)
      dut.io.operation.poke(AluOp.PASSB)
      dut.io.aluResult.expect("hDEADBEEF".U)
      dut.clock.step(1)

      // operandA should not matter
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke("h12345678".U)
      dut.io.operation.poke(AluOp.PASSB)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)
    }
  }
}