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

#include <cmath>
#include <iostream>
#include <string>
#include <utility>
#include <vector>

#include "base.h"
#include "nnet_lang_id_test_data.h"
#include "nnet_language_identifier.h"

namespace chrome_lang_id {
namespace nnet_lang_id_test {

// Tests the model on all supported languages. Returns "true" if the test is
// successful and "false" otherwise.
// TODO(abakalov): Add a test for random input that should be labeled as
// "unknown" due to low confidence.
bool TestPredictions() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  // (gold language, sample text) pairs used for testing.
  const std::vector<std::pair<std::string, std::string>> gold_lang_text = {
      {"af", NNetLangIdTestData::kTestStrAF},
      {"ar", NNetLangIdTestData::kTestStrAR},
      {"az", NNetLangIdTestData::kTestStrAZ},
      {"be", NNetLangIdTestData::kTestStrBE},
      {"bg", NNetLangIdTestData::kTestStrBG},
      {"bn", NNetLangIdTestData::kTestStrBN},
      {"bs", NNetLangIdTestData::kTestStrBS},
      {"ca", NNetLangIdTestData::kTestStrCA},
      {"ceb", NNetLangIdTestData::kTestStrCEB},
      {"cs", NNetLangIdTestData::kTestStrCS},
      {"cy", NNetLangIdTestData::kTestStrCY},
      {"da", NNetLangIdTestData::kTestStrDA},
      {"de", NNetLangIdTestData::kTestStrDE},
      {"el", NNetLangIdTestData::kTestStrEL},
      {"en", NNetLangIdTestData::kTestStrEN},
      {"eo", NNetLangIdTestData::kTestStrEO},
      {"es", NNetLangIdTestData::kTestStrES},
      {"et", NNetLangIdTestData::kTestStrET},
      {"eu", NNetLangIdTestData::kTestStrEU},
      {"fa", NNetLangIdTestData::kTestStrFA},
      {"fi", NNetLangIdTestData::kTestStrFI},
      {"fil", NNetLangIdTestData::kTestStrFIL},
      {"fr", NNetLangIdTestData::kTestStrFR},
      {"ga", NNetLangIdTestData::kTestStrGA},
      {"gl", NNetLangIdTestData::kTestStrGL},
      {"gu", NNetLangIdTestData::kTestStrGU},
      {"ha", NNetLangIdTestData::kTestStrHA},
      {"hi", NNetLangIdTestData::kTestStrHI},
      {"hmn", NNetLangIdTestData::kTestStrHMN},
      {"hr", NNetLangIdTestData::kTestStrHR},
      {"ht", NNetLangIdTestData::kTestStrHT},
      {"hu", NNetLangIdTestData::kTestStrHU},
      {"hy", NNetLangIdTestData::kTestStrHY},
      {"id", NNetLangIdTestData::kTestStrID},
      {"ig", NNetLangIdTestData::kTestStrIG},
      {"is", NNetLangIdTestData::kTestStrIS},
      {"it", NNetLangIdTestData::kTestStrIT},
      {"iw", NNetLangIdTestData::kTestStrIW},
      {"ja", NNetLangIdTestData::kTestStrJA},
      {"jv", NNetLangIdTestData::kTestStrJV},
      {"ka", NNetLangIdTestData::kTestStrKA},
      {"kk", NNetLangIdTestData::kTestStrKK},
      {"km", NNetLangIdTestData::kTestStrKM},
      {"kn", NNetLangIdTestData::kTestStrKN},
      {"ko", NNetLangIdTestData::kTestStrKO},
      {"la", NNetLangIdTestData::kTestStrLA},
      {"lo", NNetLangIdTestData::kTestStrLO},
      {"lt", NNetLangIdTestData::kTestStrLT},
      {"lv", NNetLangIdTestData::kTestStrLV},
      {"mg", NNetLangIdTestData::kTestStrMG},
      {"mi", NNetLangIdTestData::kTestStrMI},
      {"mk", NNetLangIdTestData::kTestStrMK},
      {"ml", NNetLangIdTestData::kTestStrML},
      {"mn", NNetLangIdTestData::kTestStrMN},
      {"mr", NNetLangIdTestData::kTestStrMR},
      {"ms", NNetLangIdTestData::kTestStrMS},
      {"mt", NNetLangIdTestData::kTestStrMT},
      {"my", NNetLangIdTestData::kTestStrMY},
      {"ne", NNetLangIdTestData::kTestStrNE},
      {"nl", NNetLangIdTestData::kTestStrNL},
      {"no", NNetLangIdTestData::kTestStrNO},
      {"ny", NNetLangIdTestData::kTestStrNY},
      {"pa", NNetLangIdTestData::kTestStrPA},
      {"pl", NNetLangIdTestData::kTestStrPL},
      {"pt", NNetLangIdTestData::kTestStrPT},
      {"ro", NNetLangIdTestData::kTestStrRO},
      {"ru", NNetLangIdTestData::kTestStrRU},
      {"si", NNetLangIdTestData::kTestStrSI},
      {"sk", NNetLangIdTestData::kTestStrSK},
      {"sl", NNetLangIdTestData::kTestStrSL},
      {"so", NNetLangIdTestData::kTestStrSO},
      {"sq", NNetLangIdTestData::kTestStrSQ},
      {"sr", NNetLangIdTestData::kTestStrSR},
      {"st", NNetLangIdTestData::kTestStrST},
      {"su", NNetLangIdTestData::kTestStrSU},
      {"sv", NNetLangIdTestData::kTestStrSV},
      {"sw", NNetLangIdTestData::kTestStrSW},
      {"ta", NNetLangIdTestData::kTestStrTA},
      {"te", NNetLangIdTestData::kTestStrTE},
      {"tg", NNetLangIdTestData::kTestStrTG},
      {"th", NNetLangIdTestData::kTestStrTH},
      {"tr", NNetLangIdTestData::kTestStrTR},
      {"uk", NNetLangIdTestData::kTestStrUK},
      {"ur", NNetLangIdTestData::kTestStrUR},
      {"uz", NNetLangIdTestData::kTestStrUZ},
      {"vi", NNetLangIdTestData::kTestStrVI},
      {"yi", NNetLangIdTestData::kTestStrYI},
      {"yo", NNetLangIdTestData::kTestStrYO},
      {"zh", NNetLangIdTestData::kTestStrZH},
      {"zu", NNetLangIdTestData::kTestStrZU}};

  NNetLanguageIdentifier lang_id(/*min_num_bytes=*/0,
                                 /*max_num_bytes=*/1000);

  // Iterate over all the test instances, make predictions and check that they
  // are correct.
  int num_wrong = 0;
  for (const auto &test_instance : gold_lang_text) {
    const std::string &expected_lang = test_instance.first;
    const std::string &text = test_instance.second;

    const NNetLanguageIdentifier::Result result = lang_id.FindLanguage(text);
    if (result.language != expected_lang) {
      ++num_wrong;
      std::cout << "  Misclassification: " << std::endl;
      std::cout << "    Text: " << text << std::endl;
      std::cout << "    Expected language: " << expected_lang << std::endl;
      std::cout << "    Predicted language: " << result.language << std::endl;
    }
  }

  if (num_wrong == 0) {
    std::cout << "  Success!" << std::endl;
    return true;
  } else {
    std::cout << "  Failure: " << num_wrong << " wrong predictions"
              << std::endl;
    return false;
  }
}

// Tests the model on input containing multiple languages of different scripts.
// Returns "true" if the test is successful and "false" otherwise.
bool TestMultipleLanguagesInInput() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  // Text containing snippets in English and Bulgarian.
  const std::string text =
      "This piece of text is in English. Този текст е на Български.";

  // Expected language spans in the input text, corresponding respectively to
  // Bulgarian and English.
  const std::string expected_bg_span = " Този текст е на Български ";
  const std::string expected_en_span = " This piece of text is in English ";
  const float expected_byte_sum =
      static_cast<float>(expected_bg_span.size() + expected_en_span.size());

  // Number of languages to query for and the expected byte proportions.
  const int num_queried_langs = 3;
  const std::unordered_map<string, float> expected_lang_proportions{
      {"bg", expected_bg_span.size() / expected_byte_sum},
      {"en", expected_en_span.size() / expected_byte_sum},
      {NNetLanguageIdentifier::kUnknown, 0.0}};

  NNetLanguageIdentifier lang_id(/*min_num_bytes=*/0,
                                 /*max_num_bytes=*/1000);
  const std::vector<NNetLanguageIdentifier::Result> results =
      lang_id.FindTopNMostFreqLangs(text, num_queried_langs);

  if (results.size() != expected_lang_proportions.size()) {
    std::cout << "  Failure" << std::endl;
    std::cout << "  Wrong number of languages: expected "
              << expected_lang_proportions.size() << ", obtained "
              << results.size() << std::endl;
    return false;
  }

  // Iterate over the results and check that the correct proportions are
  // returned for the expected languages.
  const float epsilon = 0.00001f;
  for (const NNetLanguageIdentifier::Result &result : results) {
    if (expected_lang_proportions.count(result.language) == 0) {
      std::cout << "  Failure" << std::endl;
      std::cout << "  Incorrect language: " << result.language << std::endl;
      return false;
    }
    if (std::abs(result.proportion -
                 expected_lang_proportions.at(result.language)) > epsilon) {
      std::cout << "  Failure" << std::endl;
      std::cout << "  Language " << result.language << ": expected proportion "
                << expected_lang_proportions.at(result.language) << ", got "
                << result.proportion << std::endl;
      return false;
    }

    // Skip over undefined language.
    if (result.language == "und")
      continue;
    if (result.byte_ranges.size() != 1) {
      std::cout << " Should only detect one span containing " << result.language
                << std::endl;
      return false;
    }
    // Check that specified byte ranges for language are correct.
    int start_index = result.byte_ranges[0].start_index;
    int end_index = result.byte_ranges[0].end_index;
    std::string byte_ranges_text = text.substr(start_index, end_index - start_index);
    if (result.language == "bg") {
      if (byte_ranges_text.compare("Този текст е на Български.") != 0) {
        std::cout << " Incorrect byte ranges returned for Bulgarian " << std::endl;
        return false;
      }
    } else if (result.language == "en") {
      if (byte_ranges_text.compare("This piece of text is in English. ") != 0) {
        std::cout << " Incorrect byte ranges returned for English " << std::endl;
        return false;
      }
    } else {
      std::cout << " Got language other than English or Bulgarian "
                << std::endl;
      return false;
    }
  }
  std::cout << "  Success!" << std::endl;
  return true;
}

}  // namespace nnet_lang_id_test
}  // namespace chrome_lang_id

// Runs tests for the language identification model.
int main(int argc, char **argv) {
  const bool tests_successful =
      chrome_lang_id::nnet_lang_id_test::TestPredictions() &&
      chrome_lang_id::nnet_lang_id_test::TestMultipleLanguagesInInput();
  return tests_successful ? 0 : 1;
}
