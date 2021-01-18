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

#include <iostream>
#include <string>

#include "base.h"
#include "nnet_language_identifier.h"

using chrome_lang_id::NNetLanguageIdentifier;

// Runs a neural net model for language identification.
int main(int argc, char **argv) {
  NNetLanguageIdentifier lang_id(/*min_num_bytes=*/0,
                                 /*max_num_bytes=*/1000);

  const std::vector<std::string> texts{"This text is written in English.",
                                       "Text in deutscher Sprache verfasst."};
  for (const std::string &text : texts) {
    const NNetLanguageIdentifier::Result result = lang_id.FindLanguage(text);
    std::cout << "text: " << text << std::endl
              << "  language: " << result.language << std::endl
              << "  probability: " << result.probability << std::endl
              << "  reliable: " << result.is_reliable << std::endl
              << "  proportion: " << result.proportion << std::endl
              << std::endl;
  }

  const std::string &text =
      "This piece of text is in English. Този текст е на Български.";
  std::cout << "text: " << text << std::endl;
  const std::vector<NNetLanguageIdentifier::Result> results =
      lang_id.FindTopNMostFreqLangs(text, /*num_langs*/ 3);
  for (const NNetLanguageIdentifier::Result &result : results) {
    std::cout << "  language: " << result.language << std::endl
              << "  probability: " << result.probability << std::endl
              << "  reliable: " << result.is_reliable << std::endl
              << "  proportion: " << result.proportion << std::endl
              << std::endl;
  }
  return 0;
}
