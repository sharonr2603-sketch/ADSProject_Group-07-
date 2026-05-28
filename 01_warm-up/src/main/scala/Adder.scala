package adder

import chisel3._
import chisel3.util._

/**
* Half Adder Class
* Each signal is one bit wide (inputs and outputs)[cite: 77, 80].
* This component has combinational behavior[cite: 108].
*/
class HalfAdder extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(Bool())
    val sum = Output(Bool())
    val carry = Output(Bool())
  })

  // Logical behavior: Sum is XOR, Carry is AND
  io.sum := io.a ^ io.b
  io.carry := io.a & io.b
}

/**
* Full Adder Class
* Implemented using two half adders and an OR gate.
*/
class FullAdder extends Module{

  val io = IO(new Bundle {

     val a = Input(Bool())
     val b = Input(Bool())
     val cin = Input(Bool())

     val sum = Output(Bool())
     val cout = Output(Bool())

     // Debug outputs for HA1
     val ha1_sum = Output(Bool())
     val ha1_carry = Output(Bool())

     // Debug outputs for HA2
     val ha2_sum = Output(Bool())
     val ha2_carry = Output(Bool())
  })

  /*
   * Instantiate two Half Adders
   */
  val ha1 = Module(new HalfAdder())
  val ha2 = Module(new HalfAdder())

  // Connections for HA1
  ha1.io.a := io.a
  ha1.io.b := io.b

  // Connections for HA2
  ha2.io.a := ha1.io.sum
  ha2.io.b := io.cin

  /*
   * Final Full Adder outputs
   */
  io.sum := ha2.io.sum
  io.cout := ha1.io.carry | ha2.io.carry

  /*
   * Debug outputs to observe HA1 and HA2 in VCD
   */
  io.ha1_sum := ha1.io.sum
  io.ha1_carry := ha1.io.carry

  io.ha2_sum := ha2.io.sum
  io.ha2_carry := ha2.io.carry
}

/**
* 4-bit Adder class
* Implements a 4-bit ripple-carry-adder manually using 4 Full Adders.
*/
class FourBitAdder extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(4.W))
    val b = Input(UInt(4.W))
    val sum = Output(UInt(4.W))
    val cout = Output(Bool())
  })

  // Instantiate the four full adders manually
  val fa1 = Module(new FullAdder())
  val fa2 = Module(new FullAdder())
  val fa3 = Module(new FullAdder())
  val fa4 = Module(new FullAdder())

  // Bit 0: Connect first bits and set initial carry-in to 0
  fa1.io.a := io.a(0)
  fa1.io.b := io.b(0)
  fa1.io.cin := false.B

  // Bit 1: Connect second bits and ripple carry from fa1
  fa2.io.a := io.a(1)
  fa2.io.b := io.b(1)
  fa2.io.cin := fa1.io.cout

  // Bit 2: Connect third bits and ripple carry from fa2
  fa3.io.a := io.a(2)
  fa3.io.b := io.b(2)
  fa3.io.cin := fa2.io.cout

  // Bit 3: Connect fourth bits and ripple carry from fa3
  fa4.io.a := io.a(3)
  fa4.io.b := io.b(3)
  fa4.io.cin := fa3.io.cout

  // Combine the individual sum bits into a 4-bit result [cite: 92, 93]
  // Cat(msb, ..., lsb) puts fa4 at the most significant position
  io.sum := Cat(fa4.io.sum, fa3.io.sum, fa2.io.sum, fa1.io.sum)
  
  // The final carry-out comes from the last adder
  io.cout := fa4.io.cout
}