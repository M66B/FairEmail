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

#include "getonescriptspan.h"

#include <iostream>
#include <vector>

namespace chrome_lang_id {
namespace CLD2 {
namespace getonescriptspan_test {

// Tests invalid and interchange-invalid input. Returns "true" if the test is
// successful and "false" otherwise.
bool TestInvalidUTF8Input() {
  std::cout << "Running " << __FUNCTION__ << std::endl;
  const std::vector<std::string> invalid_strings{"\xC0\xA9",
                                                 "\377\377\377\377"};
  const std::string gold_valid_prefix = "Some valid bytes followed by ";

  // Iterates over the invalid strings, inserts each of them in the middle of a
  // piece of text, and checks whether these strings are correctly identified.
  bool test_successful = true;
  for (size_t i = 0; i < invalid_strings.size(); ++i) {
    const std::string text = "Some valid bytes followed by " +
                             invalid_strings.at(i) +
                             " and then valid ones again.";

    const int num_valid_bytes = SpanInterchangeValid(text.c_str(), text.size());
    const std::string detected_valid_prefix(text.c_str(), num_valid_bytes);
    std::cout << "  Testing input string at position " << i << std::endl;
    if (detected_valid_prefix == gold_valid_prefix) {
      std::cout << "    Success!" << std::endl;
    } else {
      std::cout << "    Failure" << std::endl;
      std::cout << "    Gold: " << gold_valid_prefix << std::endl;
      std::cout << "    Detected: " << detected_valid_prefix << std::endl;
      test_successful = false;
    }
  }
  return test_successful;
}

// Tests whether different scripts are correctly detected. Returns "true" if the
// test is successful and "false" otherwise.
bool TestScriptDetection() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  // Text containing a snippet in English, a snippet in Bulgarian, and a snippet
  // in English again.
  const std::string text =
      "Text in English. Текст на Български. Also text in English.";
  const std::vector<std::string> gold_script_spans{
      " Text in English ", " Текст на Български ", " Also text in English "};

  std::vector<std::string> detected_script_spans;
  ScriptScanner ss(text.c_str(), text.size(), /*is_plain_text=*/true);
  LangSpan script_span;
  while (ss.GetOneScriptSpan(&script_span)) {
    detected_script_spans.emplace_back(script_span.text,
                                       script_span.text_bytes);
  }

  if (detected_script_spans.size() != gold_script_spans.size()) {
    std::cout << "  Failure" << std::endl;
    std::cout << "  Number of gold spans " << gold_script_spans.size()
              << std::endl;
    std::cout << "  Number of detected spans " << detected_script_spans.size()
              << std::endl;
    return false;
  }
  for (size_t i = 0; i < detected_script_spans.size(); ++i) {
    if (detected_script_spans.at(i) != gold_script_spans.at(i)) {
      std::cout << "  Failure" << std::endl;
      std::cout << "  Gold span: " << gold_script_spans.at(i) << std::endl;
      std::cout << "  Detected span: " << detected_script_spans.at(i)
                << std::endl;
      return false;
    }
  }
  std::cout << "  Success!" << std::endl;
  return true;
}

// Tests the case when the input string is truncated in such a way that a
// character is split in two pieces. Returns "true" if the test is successful
// and "false" otherwise.
bool TestStringCut() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  // Text in Bulgarian (Cyrillic script).
  const std::string text = "Текст на Български";

  // The size of the first two words ("Текст на ") is 16, and size of the first
  // two words plus the first char of the third word ("Текст на Б") is 18, so a
  // threshold of 17 results in slicing the first char of the third word.
  const int first_two_words_size = 16;
  const int span_size = 17;
  const int num_valid_bytes = SpanInterchangeValid(text.c_str(), span_size);
  if (num_valid_bytes == first_two_words_size) {
    std::cout << "  Success!" << std::endl;
    return true;
  } else {
    std::cout << "  Failure" << std::endl;
    std::cout << "  Size of gold interchange-valid span: "
              << first_two_words_size << std::endl;
    std::cout << "  Size of detected span: " << num_valid_bytes << std::endl;
    return false;
  }
}

}  // namespace getonescriptspan_test
}  // namespace CLD2
}  // namespace chrome_lang_id

// Runs the functions above.
int main(int argc, char **argv) {
  const bool tests_successful =
      chrome_lang_id::CLD2::getonescriptspan_test::TestInvalidUTF8Input() &&
      chrome_lang_id::CLD2::getonescriptspan_test::TestScriptDetection() &&
      chrome_lang_id::CLD2::getonescriptspan_test::TestStringCut();
  return tests_successful ? 0 : 1;
}
