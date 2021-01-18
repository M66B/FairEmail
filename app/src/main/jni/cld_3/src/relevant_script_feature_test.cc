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

#include <algorithm>
#include <cmath>
#include <iostream>
#include <memory>

#include "feature_extractor.h"
#include "feature_types.h"
#include "relevant_script_feature.h"
#include "script_detector.h"
#include "cld_3/protos/sentence.pb.h"
#include "sentence_features.h"
#include "task_context.h"
#include "utils.h"
#include "workspace.h"

namespace chrome_lang_id {
namespace relevant_script_feature_test {
namespace {
// Checks whether the expected and actual float feature values are within 0.0001
// of each other.
bool FeatureValuesNear(float expected_value, float actual_value) {
  return std::abs(expected_value - actual_value) < 0.0001;
}

// Checks whether two sets of feature values are within an acceptable amount of
// each other.
bool FeaturesNear(const string &test_input,
                  const std::map<int, float> &expected_features,
                  const std::map<int, float> &actual_features) {
  if (expected_features.size() != actual_features.size()) {
    std::cout << "  Failure for input: " << test_input << std::endl;
    return false;
  }

  for (const auto &id_and_value : expected_features) {
    const int id = id_and_value.first;
    if (actual_features.count(id) == 0 ||
        !FeatureValuesNear(expected_features.at(id), actual_features.at(id))) {
      std::cout << "  Failure for input: " << test_input << std::endl;
      return false;
    }
  }
  std::cout << "  Success for input: " << test_input << std::endl;
  return true;
}

// Checks whether the set of features is empty.
bool CheckFeaturesEmpty(const string &input,
                        const std::map<int, float> &actual_features) {
  if (!actual_features.empty()) {
    std::cout << "  Failure for input: " << input << std::endl;
    return false;
  } else {
    std::cout << "  Success for input: " << input << std::endl;
    return true;
  }
}
}  // namespace

static WholeSentenceFeature *rsf_factory() { return new RelevantScriptFeature; }

class RelevantScriptFeatureExtractor {
 public:
  RelevantScriptFeatureExtractor() {
    if (WholeSentenceFeature::registry() == nullptr) {
      // Create registry for our WholeSentenceFeature(s).
      RegisterableClass<WholeSentenceFeature>::CreateRegistry(
          "sentence feature function", "WholeSentenceFeature", __FILE__,
          __LINE__);
    }

    // Register our WholeSentenceFeature(s).
    // Register RelevantScriptFeature feature function.
    static WholeSentenceFeature::Registry::Registrar rsf_registrar(
        WholeSentenceFeature::registry(), "continuous-bag-of-relevant-scripts",
        "RelevantScriptFeature", __FILE__, __LINE__, rsf_factory);

    feature_extractor_.Parse("continuous-bag-of-relevant-scripts");
    TaskContext context;
    feature_extractor_.Setup(&context);
    feature_extractor_.Init(&context);
    feature_extractor_.RequestWorkspaces(&workspace_registry_);
  }

  // Returns "true" if feature extraction is successful, and "false" otherwise.
  bool Extract(const string &text, std::map<int, float> *float_features) {
    float_features->clear();
    if (text.empty()) {
      return true;
    }
    Sentence sentence;
    sentence.set_text(text);
    workspace_.Reset(workspace_registry_);
    feature_extractor_.Preprocess(&workspace_, &sentence);
    FeatureVector feature_vector;
    feature_extractor_.ExtractFeatures(workspace_, sentence, &feature_vector);

    for (int index = 0; index < feature_vector.size(); ++index) {
      const FloatFeatureValue value =
          FloatFeatureValue(feature_vector.value(index));
      if (float_features->count(value.value.id) != 0) {
        std::cout << "  Failure: duplicate feature" << std::endl;
        return false;
      }
      float_features->emplace(value.value.id, value.value.weight);
    }
    return true;
  }

 private:
  WorkspaceSet workspace_;
  WholeSentenceExtractor feature_extractor_;

  // The registry of shared workspaces in the feature extractor.
  WorkspaceRegistry workspace_registry_;
};

bool TestCommonCases() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  RelevantScriptFeatureExtractor extractor;
  std::map<int, float> float_features;
  bool test_successful = true;

  string input = "just some plain text";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptOtherUtf8OneByte, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  input = "ヸヂ゠ヂ";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptKatakana, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  // 4 Latin letters mixed with 4 Katakana letters.
  input = "ヸtヂe゠xtヂ";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptOtherUtf8OneByte, 0.5},
                            {chrome_lang_id::kScriptKatakana, 0.5}},
                    float_features)) {
    test_successful = false;
  }

  input = "just some 121212%^^( ヸヂ゠ヂ   text";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptOtherUtf8OneByte, 0.75},
                            {chrome_lang_id::kScriptKatakana, 0.25}},
                    float_features)) {
    test_successful = false;
  }

  return test_successful;
}

bool TestCornerCases() {
  std::cout << "Running " << __FUNCTION__ << std::endl;

  RelevantScriptFeatureExtractor extractor;
  std::map<int, float> float_features;
  bool test_successful = true;

  // Empty string.
  string input = "";
  if (!extractor.Extract(input, &float_features) ||
      !CheckFeaturesEmpty(input, float_features)) {
    test_successful = false;
  }

  // Only whitespaces.
  input = "   ";
  if (!extractor.Extract(input, &float_features) ||
      !CheckFeaturesEmpty(input, float_features)) {
    test_successful = false;
  }

  // Only numbers and punctuation.
  input = "12----)(";
  if (!extractor.Extract(input, &float_features) ||
      !CheckFeaturesEmpty(input, float_features)) {
    test_successful = false;
  }

  // Only numbers, punctuation, and spaces.
  input = "12--- - ) ( ";
  if (!extractor.Extract(input, &float_features) ||
      !CheckFeaturesEmpty(input, float_features)) {
    test_successful = false;
  }

  // One UTF8 character by itself.
  input = "ゟ";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptHiragana, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  input = "ה";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptHebrew, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  // One UTF8 character with some numbers / punctuation / spaces: character at
  // one extremity or in the middle.
  input = "1234ゟ";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptHiragana, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  input = "ゟ12-(";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptHiragana, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  input = "8*1ゟ12----";
  if (!extractor.Extract(input, &float_features) ||
      !FeaturesNear(input, {{chrome_lang_id::kScriptHiragana, 1.00}},
                    float_features)) {
    test_successful = false;
  }

  return test_successful;
}

}  // namespace relevant_script_feature_test
}  // namespace chrome_lang_id

// Runs the feature extraction tests.
int main(int argc, char **argv) {
  const bool tests_successful =
      chrome_lang_id::relevant_script_feature_test::TestCommonCases() &&
      chrome_lang_id::relevant_script_feature_test::TestCornerCases();
  return tests_successful ? 0 : 1;
}
