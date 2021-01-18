/* Copyright 2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#include "script_detector.h"

#include <iostream>

#include "utils.h"

namespace chrome_lang_id {
namespace script_detector_test {

Script GetScript(const char *p) {
  const int num_bytes = utils::OneCharLen(p);
  return chrome_lang_id::GetScript(p, num_bytes);
}

bool PrintAndReturnStatus(bool status) {
  if (status) {
    std::cout << "  Success" << std::endl;
    return true;
  } else {
    std::cout << "  Failure" << std::endl;
    return false;
  }
}

bool TestGreekScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  // The first two conditions check first / last character from the Greek and
  // Coptic script. The last two ones are negative tests.
  return PrintAndReturnStatus(
      kScriptGreek == GetScript("Ͱ") && kScriptGreek == GetScript("Ͽ") &&
      kScriptGreek == GetScript("δ") && kScriptGreek == GetScript("Θ") &&
      kScriptGreek == GetScript("Δ") && kScriptGreek != GetScript("a") &&
      kScriptGreek != GetScript("0"));
}

bool TestCyrillicScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(
      kScriptCyrillic == GetScript("Ѐ") && kScriptCyrillic == GetScript("ӿ") &&
      kScriptCyrillic == GetScript("ш") && kScriptCyrillic == GetScript("Б") &&
      kScriptCyrillic == GetScript("Ӱ"));
}

bool TestHebrewScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(
      kScriptHebrew == GetScript("֑") && kScriptHebrew == GetScript("״") &&
      kScriptHebrew == GetScript("ד") && kScriptHebrew == GetScript("ה") &&
      kScriptHebrew == GetScript("צ"));
}

bool TestArabicScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(kScriptArabic == GetScript("م") &&
                              kScriptArabic == GetScript("خ"));
}

bool TestHangulJamoScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(kScriptHangulJamo == GetScript("ᄀ") &&
                              kScriptHangulJamo == GetScript("ᇿ") &&
                              kScriptHangulJamo == GetScript("ᄡ") &&
                              kScriptHangulJamo == GetScript("ᆅ") &&
                              kScriptHangulJamo == GetScript("ᅘ"));
}

bool TestHiraganaScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(kScriptHiragana == GetScript("ぁ") &&
                              kScriptHiragana == GetScript("ゟ") &&
                              kScriptHiragana == GetScript("こ") &&
                              kScriptHiragana == GetScript("や") &&
                              kScriptHiragana == GetScript("ぜ"));
}

bool TestKatakanaScript() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  return PrintAndReturnStatus(kScriptKatakana == GetScript("゠") &&
                              kScriptKatakana == GetScript("ヿ") &&
                              kScriptKatakana == GetScript("ヂ") &&
                              kScriptKatakana == GetScript("ザ") &&
                              kScriptKatakana == GetScript("ヸ"));
}

bool TestOtherScripts() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  bool test_successful = true;

  if (kScriptOtherUtf8OneByte != GetScript("^") ||
      kScriptOtherUtf8OneByte != GetScript("$")) {
    test_successful = false;
  }

  // Unrecognized 2-byte scripts.  For info on the scripts mentioned below, see
  // http://www.unicode.org/charts/#scripts Note: the scripts below are uniquely
  // associated with a language.  Still, the number of queries in those
  // languages is small and we didn't want to increase the code size and
  // latency, so (at least for now) we do not treat them specially.
  // The following three tests are, respectively, for Armenian, Syriac and
  // Thaana.
  if (kScriptOtherUtf8TwoBytes != GetScript("Ձ") ||
      kScriptOtherUtf8TwoBytes != GetScript("ܔ") ||
      kScriptOtherUtf8TwoBytes != GetScript("ށ")) {
    test_successful = false;
  }

  // Unrecognized 3-byte script: CJK Unified Ideographs: not uniquely associated
  // with a language.
  if (kScriptOtherUtf8ThreeBytes != GetScript("万") ||
      kScriptOtherUtf8ThreeBytes != GetScript("両")) {
    test_successful = false;
  }

  // Unrecognized 4-byte script: CJK Unified Ideographs Extension C.  Note:
  // there is a nice UTF-8 encoder / decoder at https://mothereff.in/utf-8
  if (kScriptOtherUtf8FourBytes != GetScript("\xF0\xAA\x9C\x94")) {
    test_successful = false;
  }

  // Unrecognized 4-byte script: CJK Unified Ideographs Extension E
  if (kScriptOtherUtf8FourBytes != GetScript("\xF0\xAB\xA0\xB5") ||
      kScriptOtherUtf8FourBytes != GetScript("\xF0\xAC\xBA\xA1")) {
    test_successful = false;
  }

  return PrintAndReturnStatus(test_successful);
}

}  // namespace script_detector_test
}  // namespace chrome_lang_id

// Runs the feature extraction tests.
int main(int argc, char **argv) {
  const bool tests_successful =
      chrome_lang_id::script_detector_test::TestGreekScript() &&
      chrome_lang_id::script_detector_test::TestCyrillicScript() &&
      chrome_lang_id::script_detector_test::TestHebrewScript() &&
      chrome_lang_id::script_detector_test::TestArabicScript() &&
      chrome_lang_id::script_detector_test::TestHangulJamoScript() &&
      chrome_lang_id::script_detector_test::TestHiraganaScript() &&
      chrome_lang_id::script_detector_test::TestKatakanaScript() &&
      chrome_lang_id::script_detector_test::TestOtherScripts();

  return tests_successful ? 0 : 1;
}
