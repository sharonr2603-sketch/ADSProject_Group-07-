// ADS I Class Project
// Pipelined RISC-V Core - Common Definitions
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Global Definitions and Data Types

Enumerations:
    uopc: ChiselEnum defining micro-operation codes for all supported RV32I instructions:
        R-type instructions 
        I-type instructions
        NOP (no operation, default case)

This enum is used throughout the pipeline:
    Decode stage assigns uop based on instruction fields
    Execute stage maps uop to ALU operations
*/

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum

// -----------------------------------------
// Global Definitions and Data Types
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here 

object uopc extends ChiselEnum {
    
  val NOP = Value

  val ADD  = Value
  val SUB  = Value
  val SLL  = Value
  val SLT  = Value
  val SLTU = Value
  val XOR  = Value
  val SRL  = Value
  val SRA  = Value
  val OR   = Value
  val AND  = Value

  val ADDI  = Value
  val SLLI  = Value
  val SLTI  = Value
  val SLTUI = Value
  val XORI  = Value
  val ORI   = Value
  val ANDI  = Value
  val SRLI  = Value
  val SRAI  = Value

  val BEQ  = Value
  val BNE  = Value
  val BLT  = Value
  val BGE  = Value
  val BLTU = Value
  val BGEU = Value

  val JAL  = Value
  val JALR = Value
}