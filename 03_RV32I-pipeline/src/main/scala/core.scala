// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

/*
The goal of this task is to implement a 5-stage pipeline that features a subset of RV32I (all R-type and I-type instructions). 

    Instruction Memory:
        The CPU has an instruction memory (IMem) with 4096 words, each of 32 bits.
        The content of IMem is loaded from a binary file specified during the instantiation of the MultiCycleRV32Icore module.

    CPU Registers:
        The CPU has a program counter (PC) and a register file (regFile) with 32 registers, each holding a 32-bit value.
        Register x0 is hard-wired to zero.

    Microarchitectural Registers / Wires:
        Various signals are defined as either registers or wires depending on whether they need to be used in the same cycle or in a later cycle.

    Processor Stages:
        The FSM of the processor has five stages: fetch, decode, execute, memory, and writeback.
        All stages are active at the same time and process different instructions simultaneously.

        Fetch Stage:
            The instruction is fetched from the instruction memory based on the current value of the program counter (PC).

        Decode Stage:
            Instruction fields such as opcode, rd, funct3, and rs1 are extracted.
            For R-type instructions, additional fields like funct7 and rs2 are extracted.
            Control signals (isADD, isSUB, etc.) are set based on the opcode and funct3 values.
            Operands (operandA and operandB) are determined based on the instruction type.

        Execute Stage:
            Arithmetic and logic operations are performed based on the control signals and operands.
            The result is stored in the aluResult register.

        Memory Stage:
            No memory operations are implemented in this basic CPU.

        Writeback Stage:
            The result of the operation (writeBackData) is written back to the destination register (rd) in the register file.

    Check Result:
        The final result (writeBackData) is output to the io.check_res signal.
        The exception signal is also passed to the wrapper module. It indicates whether an invalid instruction has been encountered.
        In the fetch stage, a default value of 0 is assigned to io.check_res.
*/

package core_tile

import chisel3._
import chisel3.util._
import uopc._

class PipelinedRV32Icore(BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W))
    val exception = Output(Bool())
  })

  val fetch = Module(new IF(BinaryFile))
  val ifBarrier = Module(new IFBarrier)

  val decode = Module(new ID)
  val idBarrier = Module(new IDBarrier)

  val execute = Module(new EX)
  val exBarrier = Module(new EXBarrier)

  // MEM stage is only a placeholder in Assignment 3.
  // It has no IO, so we instantiate it but do not connect signals to it.
  val memory = Module(new MEM)

  val memBarrier = Module(new MEMBarrier)

  val writeback = Module(new WB)
  val wbBarrier = Module(new WBBarrier)

  val registers = Module(new regFile)

  // ---------------- IF -> IF/ID ----------------
  ifBarrier.io.inInstr := fetch.io.instr

  // ---------------- IF/ID -> ID ----------------
  decode.io.instr := ifBarrier.io.outInstr

  registers.io.req_1 <> decode.io.regFileReq_A
  registers.io.req_2 <> decode.io.regFileReq_B

  decode.io.regFileResp_A <> registers.io.resp_1
  decode.io.regFileResp_B <> registers.io.resp_2

  // ---------------- ID -> ID/EX ----------------
  idBarrier.io.inUOP := decode.io.uop
  idBarrier.io.inRD := decode.io.rd
  idBarrier.io.inOperandA := decode.io.operandA
  idBarrier.io.inOperandB := decode.io.operandB
  idBarrier.io.inXcptInvalid := decode.io.XcptInvalid

  // ---------------- ID/EX -> EX ----------------
  execute.io.uop := idBarrier.io.outUOP
  execute.io.operandA := idBarrier.io.outOperandA
  execute.io.operandB := idBarrier.io.outOperandB
  execute.io.XcptInvalid := idBarrier.io.outXcptInvalid

  // ---------------- EX -> EX/MEM ----------------
  exBarrier.io.inAluResult := execute.io.aluResult
  exBarrier.io.inRD := idBarrier.io.outRD
  exBarrier.io.inXcptInvalid := execute.io.exception

  // ---------------- MEM placeholder ----------------

  // Therefore EX/MEM barrier output is directly passed to MEM/WB barrier.
  memBarrier.io.inAluResult := exBarrier.io.outAluResult
  memBarrier.io.inRD := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcptInvalid

  // ---------------- MEM/WB -> WB ----------------
  writeback.io.aluResult := memBarrier.io.outAluResult
  writeback.io.rd := memBarrier.io.outRD
  writeback.io.exception := memBarrier.io.outException

  // ---------------- WB -> Register File ----------------
  registers.io.req_3 <> writeback.io.regFileReq

  // ---------------- WB -> WB Barrier ----------------
  wbBarrier.io.inCheckRes := writeback.io.check_res
  wbBarrier.io.inXcptInvalid := writeback.io.XcptInvalid

  // ---------------- Final Output ----------------
  io.check_res := wbBarrier.io.outCheckRes
  io.exception := wbBarrier.io.outXcptInvalid
}
