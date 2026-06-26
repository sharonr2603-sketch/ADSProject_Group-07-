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
            Arithmetic and logic operations, including branch target calculation, are performed based on the control signals and operands.
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
import chisel3.util.experimental.loadMemoryFromFile
import Assignment02.{ALU, ALUOp}
import uopc._


class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    //ToDo: Add I/O ports
    val check_res = Output(UInt(32.W))
    val exception = Output(Bool())
  })


//ToDo: Add your implementation according to the specification above here 

  val fetch = Module(new IF(BinaryFile))
  val ifBarrier = Module(new IFBarrier)

  val decode = Module(new ID)
  val idBarrier = Module(new IDbarrier)

  val execute = Module(new EX)
  val exBarrier = Module(new EXBarrier)

  val memory = Module(new MEM)
  val memBarrier = Module(new MEMBarrier)

  val writeback = Module(new WB)
  val wbBarrier = Module(new WBBarrier)

  val registers = Module(new regFile)
  val forwarding = Module(new ForwardingUnit)

  ifBarrier.io.inInstr := fetch.io.instr

  decode.io.instr := ifBarrier.io.outInstr

  registers.io.req_1 <> decode.io.regFileReq_A
  registers.io.req_2 <> decode.io.regFileReq_B
  decode.io.regFileResp_A <> registers.io.resp_1
  decode.io.regFileResp_B <> registers.io.resp_2

  idBarrier.io.inUOP := decode.io.uop
  idBarrier.io.inRD := decode.io.rd
  idBarrier.io.inRS1 := decode.io.rs1
  idBarrier.io.inRS2 := decode.io.rs2
  idBarrier.io.inOperandA := decode.io.operandA
  idBarrier.io.inOperandB := decode.io.operandB
  idBarrier.io.inXcptInvalid := decode.io.XcptInvalid

  forwarding.io.idExRs1 := idBarrier.io.outRS1
  forwarding.io.idExRs2 := idBarrier.io.outRS2
  forwarding.io.exMemRd := exBarrier.io.outRD
  forwarding.io.memWbRd := memBarrier.io.outRD
  forwarding.io.MemWrEn := (exBarrier.io.outRD =/= 0.U)
  forwarding.io.WbWrEn := (exBarrier.io.outRD =/= 0.U)

  execute.io.operandA := idBarrier.io.outOperandA
  execute.io.operandB := idBarrier.io.outOperandB

  execute.io.uop := idBarrier.io.outUOP
  execute.io.XcptInvalid := idBarrier.io.outXcptInvalid

  execute.io.forwardA := forwarding.io.forwardA
  execute.io.forwardB := forwarding.io.forwardB

  execute.io.exMemAluResult := exBarrier.io.outAluResult
  execute.io.memWbAluResult := memBarrier.io.outAluResult

  exBarrier.io.inAluResult := execute.io.aluResult
  exBarrier.io.inRD := idBarrier.io.outRD
  exBarrier.io.inXcptInvalid := execute.io.exception

  memBarrier.io.inAluResult := exBarrier.io.outAluResult
  memBarrier.io.inRD := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcptInvalid

  writeback.io.aluResult := memBarrier.io.outAluResult
  writeback.io.rd := memBarrier.io.outRD
  writeback.io.exception := memBarrier.io.outException

  registers.io.req_3 <> writeback.io.regFileReq

  wbBarrier.io.inCheckRes := writeback.io.check_res
  wbBarrier.io.inXcptInvalid := writeback.io.XcptInvalid

  io.check_res := wbBarrier.io.outCheckRes
  io.exception := wbBarrier.io.outXcptInvalid
}
