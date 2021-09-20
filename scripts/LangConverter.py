from pathlib import Path

class LangConverter:

    def __init__(self, langCode):
        self.langCode = langCode
        with open(Path("./original_lang/" + langCode + ".lang")) as f:
            print(f)
