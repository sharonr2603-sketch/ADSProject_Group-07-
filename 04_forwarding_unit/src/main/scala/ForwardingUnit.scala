// ADS I Class Project
// Pipelined RISC-V Core - Forwarding Unit
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/09/2026 by Tobias Jauch (@tojauch)

/*
Forwarding Unit: resolves data hazards by forwarding results from later pipeline stages to the ID stage

Functionality (cf. slide 6-24ff of the lecture slides):
    Detects data hazards by comparing source registers in the EX stage with destination registers in MEM and WB stages (EX and MEM barriers).
    Generates control signals for the multiplexers in the EX stage to select the correct data source for the ALU inputs
    Handles cases where multiple hazards occur simultaneously (e.g., forwarding from both MEM and WB stages)

Inputs:
    rs1_EX: source register 1 in EX stage
    rs2_EX: source register 2 in EX stage
    rd_MEM: destination register in MEM stage
    rd_WB: destination register in WB stage
    wrEn_MEM: write enable signal for MEM stage
    wrEn_WB: write enable signal for WB stage

Outputs:
    forwardA: control signal for selecting source of operand A in EX stage
    forwardB: control signal for selecting source of operand B in EX stage

*/

package core_tile

import chisel3._
import chisel3.util._
import uopc._

// -----------------------------------------
// Forwarding Unit
// -----------------------------------------

class ForwardingUnit extends Module {
  val io = IO(new Bundle {
    // Add I/O ports according to the specification above here
    val idExRs1 = Input(UInt(5.W))
    val idExRs2 = Input(UInt(5.W))

    val exMemRd = Input(UInt(5.W))
    val memWbRd = Input(UInt(5.W))

    val MemWrEn = Input(Bool())
    val WbWrEn = Input(Bool())

    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
  })

  //ToDo: Add your implementation according to the specification above here 
  io.forwardA := "b00".U
  io.forwardB := "b00".U

  when(io.MemWrEn && io.exMemRd =/= 0.U && io.exMemRd === io.idExRs1) {
    io.forwardA := "b10".U
  }.elsewhen(io.WbWrEn && io.memWbRd =/= 0.U && io.memWbRd === io.idExRs1) {
    io.forwardA := "b01".U
  }

  when(io.MemWrEn && io.exMemRd =/= 0.U && io.exMemRd === io.idExRs2) {
    io.forwardB := "b10".U
  }.elsewhen(io.WbWrEn && io.memWbRd =/= 0.U && io.memWbRd === io.idExRs2) {
    io.forwardB := "b01".U
  }
}