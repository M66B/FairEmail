// Copyright 2010 the V8 project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
//       copyright notice, this list of conditions and the following
//       disclaimer in the documentation and/or other materials provided
//       with the distribution.
//     * Neither the name of Google Inc. nor the names of its
//       contributors may be used to endorse or promote products derived
//       from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// Ported to Java from Mozilla's version of V8-dtoa by Hannes Wallnoefer.
// The original revision was 67d1049b0bf9 from the mozilla-central tree.

// Modified by Rikard Pavelic do avoid allocations
// and unused code paths due to external checks

package com.bugsnag.android.repackaged.dslplatform.json;

@SuppressWarnings("fallthrough") // suppress pre-existing warnings
abstract class Grisu3 {

	// FastDtoa will produce at most kFastDtoaMaximalLength digits.
	static final int kFastDtoaMaximalLength = 17;


	// The minimal and maximal target exponent define the range of w's binary
	// exponent, where 'w' is the result of multiplying the input by a cached power
	// of ten.
	//
	// A different range might be chosen on a different platform, to optimize digit
	// generation, but a smaller range requires more powers of ten to be cached.
	static final int minimal_target_exponent = -60;

	private static final class DiyFp {

		long f;
		int e;

		static final int kSignificandSize = 64;
		static final long kUint64MSB = 0x8000000000000000L;
		private static final long kM32 = 0xFFFFFFFFL;
		private static final long k10MSBits = 0xFFC00000L << 32;

		DiyFp() {
			this.f = 0;
			this.e = 0;
		}

		// this = this - other.
		// The exponents of both numbers must be the same and the significand of this
		// must be bigger than the significand of other.
		// The result will not be normalized.
		void subtract(DiyFp other) {
			f -= other.f;
		}

		// this = this * other.
		void multiply(DiyFp other) {
			// Simply "emulates" a 128 bit multiplication.
			// However: the resulting number only contains 64 bits. The least
			// significant 64 bits are only used for rounding the most significant 64
			// bits.
			long a = f >>> 32;
			long b = f & kM32;
			long c = other.f >>> 32;
			long d = other.f & kM32;
			long ac = a * c;
			long bc = b * c;
			long ad = a * d;
			long bd = b * d;
			long tmp = (bd >>> 32) + (ad & kM32) + (bc & kM32);
			// By adding 1U << 31 to tmp we round the final result.
			// Halfway cases will be round up.
			tmp += 1L << 31;
			long result_f = ac + (ad >>> 32) + (bc >>> 32) + (tmp >>> 32);
			e += other.e + 64;
			f = result_f;
		}

		void normalize() {
			long f = this.f;
			int e = this.e;

			// This method is mainly called for normalizing boundaries. In general
			// boundaries need to be shifted by 10 bits. We thus optimize for this case.
			while ((f & k10MSBits) == 0) {
				f <<= 10;
				e -= 10;
			}
			while ((f & kUint64MSB) == 0) {
				f <<= 1;
				e--;
			}
			this.f = f;
			this.e = e;
		}

		void reset() {
			e = 0;
			f = 0;
		}

		@Override
		public String toString() {
			return "[DiyFp f:" + f + ", e:" + e + "]";
		}

	}

	private static class CachedPowers {

		static final double kD_1_LOG2_10 = 0.30102999566398114;  //  1 / lg(10)

		static class CachedPower {
			final long significand;
			final short binaryExponent;
			final short decimalExponent;

			CachedPower(long significand, short binaryExponent, short decimalExponent) {
				this.significand = significand;
				this.binaryExponent = binaryExponent;
				this.decimalExponent = decimalExponent;
			}
		}

		static int getCachedPower(int e, int alpha, DiyFp c_mk) {
			final int kQ = DiyFp.kSignificandSize;
			final double k = Math.ceil((alpha - e + kQ - 1) * kD_1_LOG2_10);
			final int index = (GRISU_CACHE_OFFSET + (int) k - 1) / CACHED_POWERS_SPACING + 1;
			final CachedPower cachedPower = CACHED_POWERS[index];

			c_mk.f = cachedPower.significand;
			c_mk.e = cachedPower.binaryExponent;
			return cachedPower.decimalExponent;
		}

		// Code below is converted from GRISU_CACHE_NAME(8) in file "powers-ten.h"
		// Regexp to convert this from original C++ source:
		// \{GRISU_UINT64_C\((\w+), (\w+)\), (\-?\d+), (\-?\d+)\}

		// interval between entries  of the powers cache below
		static final int CACHED_POWERS_SPACING = 8;

		static final CachedPower[] CACHED_POWERS = {
				new CachedPower(0xe61acf033d1a45dfL, (short) -1087, (short) -308),
				new CachedPower(0xab70fe17c79ac6caL, (short) -1060, (short) -300),
				new CachedPower(0xff77b1fcbebcdc4fL, (short) -1034, (short) -292),
				new CachedPower(0xbe5691ef416bd60cL, (short) -1007, (short) -284),
				new CachedPower(0x8dd01fad907ffc3cL, (short) -980, (short) -276),
				new CachedPower(0xd3515c2831559a83L, (short) -954, (short) -268),
				new CachedPower(0x9d71ac8fada6c9b5L, (short) -927, (short) -260),
				new CachedPower(0xea9c227723ee8bcbL, (short) -901, (short) -252),
				new CachedPower(0xaecc49914078536dL, (short) -874, (short) -244),
				new CachedPower(0x823c12795db6ce57L, (short) -847, (short) -236),
				new CachedPower(0xc21094364dfb5637L, (short) -821, (short) -228),
				new CachedPower(0x9096ea6f3848984fL, (short) -794, (short) -220),
				new CachedPower(0xd77485cb25823ac7L, (short) -768, (short) -212),
				new CachedPower(0xa086cfcd97bf97f4L, (short) -741, (short) -204),
				new CachedPower(0xef340a98172aace5L, (short) -715, (short) -196),
				new CachedPower(0xb23867fb2a35b28eL, (short) -688, (short) -188),
				new CachedPower(0x84c8d4dfd2c63f3bL, (short) -661, (short) -180),
				new CachedPower(0xc5dd44271ad3cdbaL, (short) -635, (short) -172),
				new CachedPower(0x936b9fcebb25c996L, (short) -608, (short) -164),
				new CachedPower(0xdbac6c247d62a584L, (short) -582, (short) -156),
				new CachedPower(0xa3ab66580d5fdaf6L, (short) -555, (short) -148),
				new CachedPower(0xf3e2f893dec3f126L, (short) -529, (short) -140),
				new CachedPower(0xb5b5ada8aaff80b8L, (short) -502, (short) -132),
				new CachedPower(0x87625f056c7c4a8bL, (short) -475, (short) -124),
				new CachedPower(0xc9bcff6034c13053L, (short) -449, (short) -116),
				new CachedPower(0x964e858c91ba2655L, (short) -422, (short) -108),
				new CachedPower(0xdff9772470297ebdL, (short) -396, (short) -100),
				new CachedPower(0xa6dfbd9fb8e5b88fL, (short) -369, (short) -92),
				new CachedPower(0xf8a95fcf88747d94L, (short) -343, (short) -84),
				new CachedPower(0xb94470938fa89bcfL, (short) -316, (short) -76),
				new CachedPower(0x8a08f0f8bf0f156bL, (short) -289, (short) -68),
				new CachedPower(0xcdb02555653131b6L, (short) -263, (short) -60),
				new CachedPower(0x993fe2c6d07b7facL, (short) -236, (short) -52),
				new CachedPower(0xe45c10c42a2b3b06L, (short) -210, (short) -44),
				new CachedPower(0xaa242499697392d3L, (short) -183, (short) -36),
				new CachedPower(0xfd87b5f28300ca0eL, (short) -157, (short) -28),
				new CachedPower(0xbce5086492111aebL, (short) -130, (short) -20),
				new CachedPower(0x8cbccc096f5088ccL, (short) -103, (short) -12),
				new CachedPower(0xd1b71758e219652cL, (short) -77, (short) -4),
				new CachedPower(0x9c40000000000000L, (short) -50, (short) 4),
				new CachedPower(0xe8d4a51000000000L, (short) -24, (short) 12),
				new CachedPower(0xad78ebc5ac620000L, (short) 3, (short) 20),
				new CachedPower(0x813f3978f8940984L, (short) 30, (short) 28),
				new CachedPower(0xc097ce7bc90715b3L, (short) 56, (short) 36),
				new CachedPower(0x8f7e32ce7bea5c70L, (short) 83, (short) 44),
				new CachedPower(0xd5d238a4abe98068L, (short) 109, (short) 52),
				new CachedPower(0x9f4f2726179a2245L, (short) 136, (short) 60),
				new CachedPower(0xed63a231d4c4fb27L, (short) 162, (short) 68),
				new CachedPower(0xb0de65388cc8ada8L, (short) 189, (short) 76),
				new CachedPower(0x83c7088e1aab65dbL, (short) 216, (short) 84),
				new CachedPower(0xc45d1df942711d9aL, (short) 242, (short) 92),
				new CachedPower(0x924d692ca61be758L, (short) 269, (short) 100),
				new CachedPower(0xda01ee641a708deaL, (short) 295, (short) 108),
				new CachedPower(0xa26da3999aef774aL, (short) 322, (short) 116),
				new CachedPower(0xf209787bb47d6b85L, (short) 348, (short) 124),
				new CachedPower(0xb454e4a179dd1877L, (short) 375, (short) 132),
				new CachedPower(0x865b86925b9bc5c2L, (short) 402, (short) 140),
				new CachedPower(0xc83553c5c8965d3dL, (short) 428, (short) 148),
				new CachedPower(0x952ab45cfa97a0b3L, (short) 455, (short) 156),
				new CachedPower(0xde469fbd99a05fe3L, (short) 481, (short) 164),
				new CachedPower(0xa59bc234db398c25L, (short) 508, (short) 172),
				new CachedPower(0xf6c69a72a3989f5cL, (short) 534, (short) 180),
				new CachedPower(0xb7dcbf5354e9beceL, (short) 561, (short) 188),
				new CachedPower(0x88fcf317f22241e2L, (short) 588, (short) 196),
				new CachedPower(0xcc20ce9bd35c78a5L, (short) 614, (short) 204),
				new CachedPower(0x98165af37b2153dfL, (short) 641, (short) 212),
				new CachedPower(0xe2a0b5dc971f303aL, (short) 667, (short) 220),
				new CachedPower(0xa8d9d1535ce3b396L, (short) 694, (short) 228),
				new CachedPower(0xfb9b7cd9a4a7443cL, (short) 720, (short) 236),
				new CachedPower(0xbb764c4ca7a44410L, (short) 747, (short) 244),
				new CachedPower(0x8bab8eefb6409c1aL, (short) 774, (short) 252),
				new CachedPower(0xd01fef10a657842cL, (short) 800, (short) 260),
				new CachedPower(0x9b10a4e5e9913129L, (short) 827, (short) 268),
				new CachedPower(0xe7109bfba19c0c9dL, (short) 853, (short) 276),
				new CachedPower(0xac2820d9623bf429L, (short) 880, (short) 284),
				new CachedPower(0x80444b5e7aa7cf85L, (short) 907, (short) 292),
				new CachedPower(0xbf21e44003acdd2dL, (short) 933, (short) 300),
				new CachedPower(0x8e679c2f5e44ff8fL, (short) 960, (short) 308),
				new CachedPower(0xd433179d9c8cb841L, (short) 986, (short) 316),
				new CachedPower(0x9e19db92b4e31ba9L, (short) 1013, (short) 324),
				new CachedPower(0xeb96bf6ebadf77d9L, (short) 1039, (short) 332),
				new CachedPower(0xaf87023b9bf0ee6bL, (short) 1066, (short) 340)
		};

		// nb elements (8): 82

		static final int GRISU_CACHE_OFFSET = 308;
	}

	private static class DoubleHelper {

		static final long kExponentMask = 0x7FF0000000000000L;
		static final long kSignificandMask = 0x000FFFFFFFFFFFFFL;
		static final long kHiddenBit = 0x0010000000000000L;

		static void asDiyFp(long d64, DiyFp v) {
			v.f = significand(d64);
			v.e = exponent(d64);
		}

		// this->Significand() must not be 0.
		static void asNormalizedDiyFp(long d64, DiyFp w) {
			long f = significand(d64);
			int e = exponent(d64);

			// The current double could be a denormal.
			while ((f & kHiddenBit) == 0) {
				f <<= 1;
				e--;
			}
			// Do the final shifts in one go. Don't forget the hidden bit (the '-1').
			f <<= DiyFp.kSignificandSize - kSignificandSize - 1;
			e -= DiyFp.kSignificandSize - kSignificandSize - 1;
			w.f = f;
			w.e = e;
		}

		static int exponent(long d64) {
			if (isDenormal(d64)) return kDenormalExponent;

			int biased_e = (int) (((d64 & kExponentMask) >>> kSignificandSize) & 0xffffffffL);
			return biased_e - kExponentBias;
		}

		static long significand(long d64) {
			long significand = d64 & kSignificandMask;
			if (!isDenormal(d64)) {
				return significand + kHiddenBit;
			} else {
				return significand;
			}
		}

		// Returns true if the double is a denormal.
		private static boolean isDenormal(long d64) {
			return (d64 & kExponentMask) == 0L;
		}

		// Returns the two boundaries of first argument.
		// The bigger boundary (m_plus) is normalized. The lower boundary has the same
		// exponent as m_plus.
		static void normalizedBoundaries(DiyFp v, long d64, DiyFp m_minus, DiyFp m_plus) {
			asDiyFp(d64, v);
			final boolean significand_is_zero = (v.f == kHiddenBit);
			m_plus.f = (v.f << 1) + 1;
			m_plus.e = v.e - 1;
			m_plus.normalize();
			if (significand_is_zero && v.e != kDenormalExponent) {
				// The boundary is closer. Think of v = 1000e10 and v- = 9999e9.
				// Then the boundary (== (v - v-)/2) is not just at a distance of 1e9 but
				// at a distance of 1e8.
				// The only exception is for the smallest normal: the largest denormal is
				// at the same distance as its successor.
				// Note: denormals have the same exponent as the smallest normals.
				m_minus.f = (v.f << 2) - 1;
				m_minus.e = v.e - 2;
			} else {
				m_minus.f = (v.f << 1) - 1;
				m_minus.e = v.e - 1;
			}
			m_minus.f = m_minus.f << (m_minus.e - m_plus.e);
			m_minus.e = m_plus.e;
		}

		private static final int kSignificandSize = 52;  // Excludes the hidden bit.
		private static final int kExponentBias = 0x3FF + kSignificandSize;
		private static final int kDenormalExponent = -kExponentBias + 1;

	}

	static class FastDtoa {

		// Adjusts the last digit of the generated number, and screens out generated
		// solutions that may be inaccurate. A solution may be inaccurate if it is
		// outside the safe interval, or if we ctannot prove that it is closer to the
		// input than a neighboring representation of the same length.
		//
		// Input: * buffer containing the digits of too_high / 10^kappa
		//        * distance_too_high_w == (too_high - w).f() * unit
		//        * unsafe_interval == (too_high - too_low).f() * unit
		//        * rest = (too_high - buffer * 10^kappa).f() * unit
		//        * ten_kappa = 10^kappa * unit
		//        * unit = the common multiplier
		// Output: returns true if the buffer is guaranteed to contain the closest
		//    representable number to the input.
		//  Modifies the generated digits in the buffer to approach (round towards) w.
		static boolean roundWeed(
				final FastDtoaBuilder buffer,
				final long distance_too_high_w,
				final long unsafe_interval,
				long rest,
				final long ten_kappa,
				final long unit) {
			final long small_distance = distance_too_high_w - unit;
			final long big_distance = distance_too_high_w + unit;
			// Let w_low  = too_high - big_distance, and
			//     w_high = too_high - small_distance.
			// Note: w_low < w < w_high
			//
			// The real w (* unit) must lie somewhere inside the interval
			// ]w_low; w_low[ (often written as "(w_low; w_low)")

			// Basically the buffer currently contains a number in the unsafe interval
			// ]too_low; too_high[ with too_low < w < too_high
			//
			//  too_high - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			//                     ^v 1 unit            ^      ^                 ^      ^
			//  boundary_high ---------------------     .      .                 .      .
			//                     ^v 1 unit            .      .                 .      .
			//   - - - - - - - - - - - - - - - - - - -  +  - - + - - - - - -     .      .
			//                                          .      .         ^       .      .
			//                                          .  big_distance  .       .      .
			//                                          .      .         .       .    rest
			//                              small_distance     .         .       .      .
			//                                          v      .         .       .      .
			//  w_high - - - - - - - - - - - - - - - - - -     .         .       .      .
			//                     ^v 1 unit                   .         .       .      .
			//  w ----------------------------------------     .         .       .      .
			//                     ^v 1 unit                   v         .       .      .
			//  w_low  - - - - - - - - - - - - - - - - - - - - -         .       .      .
			//                                                           .       .      v
			//  buffer --------------------------------------------------+-------+--------
			//                                                           .       .
			//                                                  safe_interval    .
			//                                                           v       .
			//   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -     .
			//                     ^v 1 unit                                     .
			//  boundary_low -------------------------                     unsafe_interval
			//                     ^v 1 unit                                     v
			//  too_low  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			//
			//
			// Note that the value of buffer could lie anywhere inside the range too_low
			// to too_high.
			//
			// boundary_low, boundary_high and w are approximations of the real boundaries
			// and v (the input number). They are guaranteed to be precise up to one unit.
			// In fact the error is guaranteed to be strictly less than one unit.
			//
			// Anything that lies outside the unsafe interval is guaranteed not to round
			// to v when read again.
			// Anything that lies inside the safe interval is guaranteed to round to v
			// when read again.
			// If the number inside the buffer lies inside the unsafe interval but not
			// inside the safe interval then we simply do not know and bail out (returning
			// false).
			//
			// Similarly we have to take into account the imprecision of 'w' when rounding
			// the buffer. If we have two potential representations we need to make sure
			// that the chosen one is closer to w_low and w_high since v can be anywhere
			// between them.
			//
			// By generating the digits of too_high we got the largest (closest to
			// too_high) buffer that is still in the unsafe interval. In the case where
			// w_high < buffer < too_high we try to decrement the buffer.
			// This way the buffer approaches (rounds towards) w.
			// There are 3 conditions that stop the decrementation process:
			//   1) the buffer is already below w_high
			//   2) decrementing the buffer would make it leave the unsafe interval
			//   3) decrementing the buffer would yield a number below w_high and farther
			//      away than the current number. In other words:
			//              (buffer{-1} < w_high) && w_high - buffer{-1} > buffer - w_high
			// Instead of using the buffer directly we use its distance to too_high.
			// Conceptually rest ~= too_high - buffer
			while (rest < small_distance &&  // Negated condition 1
					unsafe_interval - rest >= ten_kappa &&  // Negated condition 2
					(rest + ten_kappa < small_distance ||  // buffer{-1} > w_high
							small_distance - rest >= rest + ten_kappa - small_distance)) {
				buffer.decreaseLast();
				rest += ten_kappa;
			}

			// We have approached w+ as much as possible. We now test if approaching w-
			// would require changing the buffer. If yes, then we have two possible
			// representations close to w, but we cannot decide which one is closer.
			if (rest < big_distance &&
					unsafe_interval - rest >= ten_kappa &&
					(rest + ten_kappa < big_distance ||
							big_distance - rest > rest + ten_kappa - big_distance)) {
				return false;
			}

			// Weeding test.
			//   The safe interval is [too_low + 2 ulp; too_high - 2 ulp]
			//   Since too_low = too_high - unsafe_interval this is equivalent to
			//      [too_high - unsafe_interval + 4 ulp; too_high - 2 ulp]
			//   Conceptually we have: rest ~= too_high - buffer
			return (2 * unit <= rest) && (rest <= unsafe_interval - 4 * unit);
		}

		static final int kTen4 = 10000;
		static final int kTen5 = 100000;
		static final int kTen6 = 1000000;
		static final int kTen7 = 10000000;
		static final int kTen8 = 100000000;
		static final int kTen9 = 1000000000;

		// Returns the biggest power of ten that is less than or equal than the given
		// number. We furthermore receive the maximum number of bits 'number' has.
		// If number_bits == 0 then 0^-1 is returned
		// The number of bits must be <= 32.
		// Precondition: (1 << number_bits) <= number < (1 << (number_bits + 1)).
		static long biggestPowerTen(int number, int number_bits) {
			int power, exponent;
			switch (number_bits) {
				case 32:
				case 31:
				case 30:
					if (kTen9 <= number) {
						power = kTen9;
						exponent = 9;
						break;
					}  // else fallthrough
				case 29:
				case 28:
				case 27:
					if (kTen8 <= number) {
						power = kTen8;
						exponent = 8;
						break;
					}  // else fallthrough
				case 26:
				case 25:
				case 24:
					if (kTen7 <= number) {
						power = kTen7;
						exponent = 7;
						break;
					}  // else fallthrough
				case 23:
				case 22:
				case 21:
				case 20:
					if (kTen6 <= number) {
						power = kTen6;
						exponent = 6;
						break;
					}  // else fallthrough
				case 19:
				case 18:
				case 17:
					if (kTen5 <= number) {
						power = kTen5;
						exponent = 5;
						break;
					}  // else fallthrough
				case 16:
				case 15:
				case 14:
					if (kTen4 <= number) {
						power = kTen4;
						exponent = 4;
						break;
					}  // else fallthrough
				case 13:
				case 12:
				case 11:
				case 10:
					if (1000 <= number) {
						power = 1000;
						exponent = 3;
						break;
					}  // else fallthrough
				case 9:
				case 8:
				case 7:
					if (100 <= number) {
						power = 100;
						exponent = 2;
						break;
					}  // else fallthrough
				case 6:
				case 5:
				case 4:
					if (10 <= number) {
						power = 10;
						exponent = 1;
						break;
					}  // else fallthrough
				case 3:
				case 2:
				case 1:
					if (1 <= number) {
						power = 1;
						exponent = 0;
						break;
					}  // else fallthrough
				case 0:
					power = 0;
					exponent = -1;
					break;
				default:
					// Following assignments are here to silence compiler warnings.
					power = 0;
					exponent = 0;
					// UNREACHABLE();
			}
			return ((long) power << 32) | (0xffffffffL & exponent);
		}

		// Generates the digits of input number w.
		// w is a floating-point number (DiyFp), consisting of a significand and an
		// exponent. Its exponent is bounded by minimal_target_exponent and
		// maximal_target_exponent.
		//       Hence -60 <= w.e() <= -32.
		//
		// Returns false if it fails, in which case the generated digits in the buffer
		// should not be used.
		// Preconditions:
		//  * low, w and high are correct up to 1 ulp (unit in the last place). That
		//    is, their error must be less that a unit of their last digits.
		//  * low.e() == w.e() == high.e()
		//  * low < w < high, and taking into account their error: low~ <= high~
		//  * minimal_target_exponent <= w.e() <= maximal_target_exponent
		// Postconditions: returns false if procedure fails.
		//   otherwise:
		//     * buffer is not null-terminated, but len contains the number of digits.
		//     * buffer contains the shortest possible decimal digit-sequence
		//       such that LOW < buffer * 10^kappa < HIGH, where LOW and HIGH are the
		//       correct values of low and high (without their error).
		//     * if more than one decimal representation gives the minimal number of
		//       decimal digits then the one closest to W (where W is the correct value
		//       of w) is chosen.
		// Remark: this procedure takes into account the imprecision of its input
		//   numbers. If the precision is not enough to guarantee all the postconditions
		//   then false is returned. This usually happens rarely (~0.5%).
		//
		// Say, for the sake of example, that
		//   w.e() == -48, and w.f() == 0x1234567890abcdef
		// w's value can be computed by w.f() * 2^w.e()
		// We can obtain w's integral digits by simply shifting w.f() by -w.e().
		//  -> w's integral part is 0x1234
		//  w's fractional part is therefore 0x567890abcdef.
		// Printing w's integral part is easy (simply print 0x1234 in decimal).
		// In order to print its fraction we repeatedly multiply the fraction by 10 and
		// get each digit. Example the first digit after the point would be computed by
		//   (0x567890abcdef * 10) >> 48. -> 3
		// The whole thing becomes slightly more complicated because we want to stop
		// once we have enough digits. That is, once the digits inside the buffer
		// represent 'w' we can stop. Everything inside the interval low - high
		// represents w. However we have to pay attention to low, high and w's
		// imprecision.
		static boolean digitGen(FastDtoaBuilder buffer, int mk) {
			final DiyFp low = buffer.scaled_boundary_minus;
			final DiyFp w = buffer.scaled_w;
			final DiyFp high = buffer.scaled_boundary_plus;

			// low, w and high are imprecise, but by less than one ulp (unit in the last
			// place).
			// If we remove (resp. add) 1 ulp from low (resp. high) we are certain that
			// the new numbers are outside of the interval we want the final
			// representation to lie in.
			// Inversely adding (resp. removing) 1 ulp from low (resp. high) would yield
			// numbers that are certain to lie in the interval. We will use this fact
			// later on.
			// We will now start by generating the digits within the uncertain
			// interval. Later we will weed out representations that lie outside the safe
			// interval and thus _might_ lie outside the correct interval.
			long unit = 1;
			final DiyFp too_low = buffer.too_low;
			too_low.f = low.f - unit;
			too_low.e = low.e;
			final DiyFp too_high = buffer.too_high;
			too_high.f = high.f + unit;
			too_high.e = high.e;
			// too_low and too_high are guaranteed to lie outside the interval we want the
			// generated number in.
			final DiyFp unsafe_interval = buffer.unsafe_interval;
			unsafe_interval.f = too_high.f;
			unsafe_interval.e = too_high.e;
			unsafe_interval.subtract(too_low);
			// We now cut the input number into two parts: the integral digits and the
			// fractionals. We will not write any decimal separator though, but adapt
			// kappa instead.
			// Reminder: we are currently computing the digits (stored inside the buffer)
			// such that:   too_low < buffer * 10^kappa < too_high
			// We use too_high for the digit_generation and stop as soon as possible.
			// If we stop early we effectively round down.
			final DiyFp one = buffer.one;
			one.f = 1L << -w.e;
			one.e = w.e;
			// Division by one is a shift.
			int integrals = (int) ((too_high.f >>> -one.e) & 0xffffffffL);
			// Modulo by one is an and.
			long fractionals = too_high.f & (one.f - 1);
			long result = biggestPowerTen(integrals, DiyFp.kSignificandSize - (-one.e));
			int divider = (int) ((result >>> 32) & 0xffffffffL);
			int divider_exponent = (int) (result & 0xffffffffL);
			int kappa = divider_exponent + 1;
			// Loop invariant: buffer = too_high / 10^kappa  (integer division)
			// The invariant holds for the first iteration: kappa has been initialized
			// with the divider exponent + 1. And the divider is the biggest power of ten
			// that is smaller than integrals.
			while (kappa > 0) {
				int digit = integrals / divider;
				buffer.append((byte) ('0' + digit));
				integrals %= divider;
				kappa--;
				// Note that kappa now equals the exponent of the divider and that the
				// invariant thus holds again.
				final long rest = ((long) integrals << -one.e) + fractionals;
				// Invariant: too_high = buffer * 10^kappa + DiyFp(rest, one.e())
				// Reminder: unsafe_interval.e() == one.e()
				if (rest < unsafe_interval.f) {
					// Rounding down (by not emitting the remaining digits) yields a number
					// that lies within the unsafe interval.
					buffer.point = buffer.end - mk + kappa;
					final DiyFp minus_round = buffer.minus_round;
					minus_round.f = too_high.f;
					minus_round.e = too_high.e;
					minus_round.subtract(w);
					return roundWeed(buffer, minus_round.f,
							unsafe_interval.f, rest,
							(long) divider << -one.e, unit);
				}
				divider /= 10;
			}

			// The integrals have been generated. We are at the point of the decimal
			// separator. In the following loop we simply multiply the remaining digits by
			// 10 and divide by one. We just need to pay attention to multiply associated
			// data (like the interval or 'unit'), too.
			// Instead of multiplying by 10 we multiply by 5 (cheaper operation) and
			// increase its (imaginary) exponent. At the same time we decrease the
			// divider's (one's) exponent and shift its significand.
			// Basically, if fractionals was a DiyFp (with fractionals.e == one.e):
			//      fractionals.f *= 10;
			//      fractionals.f >>= 1; fractionals.e++; // value remains unchanged.
			//      one.f >>= 1; one.e++;                 // value remains unchanged.
			//      and we have again fractionals.e == one.e which allows us to divide
			//           fractionals.f() by one.f()
			// We simply combine the *= 10 and the >>= 1.
			while (true) {
				fractionals *= 5;
				unit *= 5;
				unsafe_interval.f = unsafe_interval.f * 5;
				unsafe_interval.e = unsafe_interval.e + 1;  // Will be optimized out.
				one.f = one.f >>> 1;
				one.e = one.e + 1;
				// Integer division by one.
				final int digit = (int) ((fractionals >>> -one.e) & 0xffffffffL);
				buffer.append((byte) ('0' + digit));
				fractionals &= one.f - 1;  // Modulo by one.
				kappa--;
				if (fractionals < unsafe_interval.f) {
					buffer.point = buffer.end - mk + kappa;
					final DiyFp minus_round = buffer.minus_round;
					minus_round.f = too_high.f;
					minus_round.e = too_high.e;
					minus_round.subtract(w);
					return roundWeed(buffer, minus_round.f * unit,
							unsafe_interval.f, fractionals, one.f, unit);
				}
			}
		}
	}

	public static boolean tryConvert(final double value, final FastDtoaBuilder buffer) {
		final long bits;
		final int firstDigit;
		buffer.reset();
		if (value < 0) {
			buffer.append((byte) '-');
			bits = Double.doubleToLongBits(-value);
			firstDigit = 1;
		} else {
			bits = Double.doubleToLongBits(value);
			firstDigit = 0;
		}

		// Provides a decimal representation of v.
		// Returns true if it succeeds, otherwise the result cannot be trusted.
		// There will be *length digits inside the buffer (not null-terminated).
		// If the function returns true then
		//        v == (double) (buffer * 10^decimal_exponent).
		// The digits in the buffer are the shortest representation possible: no
		// 0.09999999999999999 instead of 0.1. The shorter representation will even be
		// chosen even if the longer one would be closer to v.
		// The last digit will be closest to the actual v. That is, even if several
		// digits might correctly yield 'v' when read again, the closest will be
		// computed.
		final int mk = buffer.initialize(bits);

		// DigitGen will generate the digits of scaled_w. Therefore we have
		// v == (double) (scaled_w * 10^-mk).
		// Set decimal_exponent == -mk and pass it to DigitGen. If scaled_w is not an
		// integer than it will be updated. For instance if scaled_w == 1.23 then
		// the buffer will be filled with "123" und the decimal_exponent will be
		// decreased by 2.
		if (FastDtoa.digitGen(buffer, mk)) {
			buffer.write(firstDigit);
			return true;
		} else {
			return false;
		}
	}

	static class FastDtoaBuilder {

		private final DiyFp v = new DiyFp();
		private final DiyFp w = new DiyFp();
		private final DiyFp boundary_minus = new DiyFp();
		private final DiyFp boundary_plus = new DiyFp();
		private final DiyFp ten_mk = new DiyFp();
		private final DiyFp scaled_w = new DiyFp();
		private final DiyFp scaled_boundary_minus = new DiyFp();
		private final DiyFp scaled_boundary_plus = new DiyFp();

		private final DiyFp too_low = new DiyFp();
		private final DiyFp too_high = new DiyFp();
		private final DiyFp unsafe_interval = new DiyFp();
		private final DiyFp one = new DiyFp();
		private final DiyFp minus_round = new DiyFp();

		int initialize(final long bits) {
			DoubleHelper.asNormalizedDiyFp(bits, w);
			// boundary_minus and boundary_plus are the boundaries between v and its
			// closest floating-point neighbors. Any number strictly between
			// boundary_minus and boundary_plus will round to v when convert to a double.
			// Grisu3 will never output representations that lie exactly on a boundary.
			boundary_minus.reset();
			boundary_plus.reset();
			DoubleHelper.normalizedBoundaries(v, bits, boundary_minus, boundary_plus);
			ten_mk.reset(); // Cached power of ten: 10^-k
			final int mk = CachedPowers.getCachedPower(w.e + DiyFp.kSignificandSize, minimal_target_exponent, ten_mk);
			// Note that ten_mk is only an approximation of 10^-k. A DiyFp only contains a
			// 64 bit significand and ten_mk is thus only precise up to 64 bits.

			// The DiyFp::Times procedure rounds its result, and ten_mk is approximated
			// too. The variable scaled_w (as well as scaled_boundary_minus/plus) are now
			// off by a small amount.
			// In fact: scaled_w - w*10^k < 1ulp (unit in the last place) of scaled_w.
			// In other words: let f = scaled_w.f() and e = scaled_w.e(), then
			//           (f-1) * 2^e < w*10^k < (f+1) * 2^e
			scaled_w.f = w.f;
			scaled_w.e = w.e;
			scaled_w.multiply(ten_mk);
			// In theory it would be possible to avoid some recomputations by computing
			// the difference between w and boundary_minus/plus (a power of 2) and to
			// compute scaled_boundary_minus/plus by subtracting/adding from
			// scaled_w. However the code becomes much less readable and the speed
			// enhancements are not terriffic.
			scaled_boundary_minus.f = boundary_minus.f;
			scaled_boundary_minus.e = boundary_minus.e;
			scaled_boundary_minus.multiply(ten_mk);
			scaled_boundary_plus.f = boundary_plus.f;
			scaled_boundary_plus.e = boundary_plus.e;
			scaled_boundary_plus.multiply(ten_mk);

			return mk;
		}

		// allocate buffer for generated digits + extra notation + padding zeroes
		private final byte[] chars = new byte[kFastDtoaMaximalLength + 10];
		private int end = 0;
		private int point;

		void reset() {
			end = 0;
		}

		void append(byte c) {
			chars[end++] = c;
		}

		void decreaseLast() {
			chars[end - 1]--;
		}

		@Override
		public String toString() {
			return "[chars:" + new String(chars, 0, end) + ", point:" + point + "]";
		}

		int copyTo(final byte[] target, final int position) {
			for (int i = 0; i < end; i++) {
				target[i + position] = chars[i];
			}
			return end;
		}

		public void write(int firstDigit) {
			// check for minus sign
			int decPoint = point - firstDigit;
			if (decPoint < -5 || decPoint > 21) {
				toExponentialFormat(firstDigit, decPoint);
			} else {
				toFixedFormat(firstDigit, decPoint);
			}
		}

		private void toFixedFormat(int firstDigit, int decPoint) {
			if (point < end) {
				// insert decimal point
				if (decPoint > 0) {
					// >= 1, split decimals and insert point
					for (int i = end; i >= point; i--) {
						chars[i + 1] = chars[i];
					}
					chars[point] = '.';
					end++;
				} else {
					// < 1,
					final int offset = 2 - decPoint;
					for (int i = end + firstDigit; i >= firstDigit; i--) {
						chars[i + offset] = chars[i];
					}
					chars[firstDigit] = '0';
					chars[firstDigit + 1] = '.';
					if (decPoint < 0) {
						int target = firstDigit + 2 - decPoint;
						for (int i = firstDigit + 2; i < target; i++) {
							chars[i] = '0';
						}
					}
					end += 2 - decPoint;
				}
			} else if (point > end) {
				// large integer, add trailing zeroes
				for (int i = end; i < point; i++) {
					chars[i] = '0';
				}
				end += point - end;
				chars[end] = '.';
				chars[end + 1] = '0';
				end += 2;
			} else {
				chars[end] = '.';
				chars[end + 1] = '0';
				end += 2;
			}
		}

		private void toExponentialFormat(int firstDigit, int decPoint) {
			if (end - firstDigit > 1) {
				// insert decimal point if more than one digit was produced
				int dot = firstDigit + 1;
				System.arraycopy(chars, dot, chars, dot + 1, end - dot);
				chars[dot] = '.';
				end++;
			}
			chars[end++] = 'E';
			byte sign = '+';
			int exp = decPoint - 1;
			if (exp < 0) {
				sign = '-';
				exp = -exp;
			}
			chars[end++] = sign;

			int charPos = exp > 99 ? end + 2 : exp > 9 ? end + 1 : end;
			end = charPos + 1;

			do {
				int r = exp % 10;
				chars[charPos--] = digits[r];
				exp = exp / 10;
			} while (exp != 0);
		}

		final static byte[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	}
}
