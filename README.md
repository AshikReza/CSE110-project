# StudyAI (Easiest Console Version)

A console-based, Anki-style flashcard maker for your OOP project.
No AI API is called anywhere — you generate a prompt, paste it into any
chatbot (ChatGPT, Gemini, Claude...), then paste the answer back in.

This is the simplest version: **5 files, ~600 lines total, no regular
expressions.** All features from the earlier versions are still here.

## How to run

You need a JDK installed (Java 17+).

```
cd StudyAI-Easy
javac -d out src/studyai/*.java
java -cp out studyai.Main
```

No build tools, no dependencies — just `javac` and `java`.

## How to use it

1. Choose **1** to generate a prompt. Copy the text it prints and paste it
   into ChatGPT/Gemini/Claude.
2. Copy the AI's reply, choose **2**, paste it in, then type `END` on its
   own line. Each field must be on its own line, e.g.:
   ```
   Question: Which keyword is used to inherit a class in Java?
   Type: MCQ
   Option A: implements
   Option B: extends
   Option C: inherits
   Option D: super
   Answer: B
   Explanation: extends is used for class inheritance.
   ---
   ```
3. Use **3** to study, **4** to take a quiz, **5-8** to search/edit/
   delete/bookmark cards, **9** to shuffle, **10/11** to save/load the
   deck to a file.

## Files (5 total, one package: `studyai`)

| File | What's inside |
|---|---|
| `Main.java` | The whole console menu / UI |
| `Question.java` | `Question` (abstract) + `MCQQuestion` + `TrueFalseQuestion` + `FillBlankQuestion` — all 4 classes in one file |
| `Deck.java` | Holds `ArrayList<Question>`; add/remove/search/shuffle/save/load |
| `PromptTools.java` | `PromptGenerator` + `Parser` — building the prompt and reading the AI's reply back, both in one file |
| `InvalidPromptFormatException.java` | The one required custom exception |

Java allows several classes in the same `.java` file as long as only one
of them is `public` — that's how 9 classes fit into 5 files without
losing any of the OOP structure.

## What changed from the previous version

- `Question`'s 3 subclasses moved into `Question.java` instead of their
  own files.
- `PromptGenerator` and `Parser` moved into one `PromptTools.java` file.
- **`Parser` no longer uses regular expressions.** It just splits the
  pasted text into blocks on `---` lines, then checks each line with
  `line.startsWith("Question:")`-style comparisons. Easier to read and
  easier to explain in a viva.
- The prompt format is now "one field per line" (`Question: ...` on a
  single line) instead of "label, then a line break, then the value" —
  simpler for both the AI to follow and the parser to read.

*Trade-off to know about:* because the parser only reads one line per
field, a very long AI answer that wraps onto a second line will get cut
off. In practice AI chatbots keep these short enough that this rarely
happens, but it's worth mentioning if your instructor asks about
limitations.

## OOP concepts covered

- **Abstract class + inheritance**: `Question` → `MCQQuestion` /
  `TrueFalseQuestion` / `FillBlankQuestion`
- **Polymorphism**: `Main` calls `q.display()`, `q.checkAnswer(...)`,
  `q.showAnswer()` on a plain `Question` reference — the correct
  override runs based on the object's real type at runtime (see
  `studyFlashcards()` and `startQuiz()` in `Main.java`)
- **Collections**: `ArrayList<Question>` inside `Deck`
- **Custom exception**: `InvalidPromptFormatException`, thrown by `Parser`
- **Built-in exceptions handled**: `NumberFormatException` (menu input),
  `IllegalArgumentException` (bad constructor input, bad True/False
  answer), `IOException` / `ClassNotFoundException` (save/load)
- **File handling**: `Deck implements Serializable`; `saveToFile` /
  `loadFromFile` use `ObjectOutputStream` / `ObjectInputStream`

## Class diagram

See `StudyAI_Easy_ClassDiagram.puml` in this folder. Paste its contents
into [plantuml.com/plantuml](https://www.plantuml.com/plantuml/uml/) or
[planttext.com](https://www.planttext.com/) to render it as an image, or
use the PlantUML extension in VS Code to preview it locally.

```
        Question (abstract)              <- Question.java
       /      |        \
MCQQuestion  TrueFalseQuestion  FillBlankQuestion

Deck  --(has many)-->  Question           <- Deck.java
Main  --(uses)-->  Deck, PromptGenerator, Parser   <- Main.java, PromptTools.java
Parser --(throws)--> InvalidPromptFormatException  <- InvalidPromptFormatException.java
```

## Viva-ready talking points

- **Why is `Question` abstract?** So every question type is forced to
  supply its own `checkAnswer` logic — no giant if/else on a "type"
  field anywhere in the code.
- **Where's the polymorphism?** In `Main.startQuiz()`: the loop variable
  is typed `Question`, but `q.checkAnswer(userAnswer)` runs the correct
  subclass's version automatically.
- **Why no AI API?** `PromptGenerator` only builds a `String`.
  `Parser` only reads a `String`. Neither ever makes a network call —
  you copy/paste manually, satisfying the "no API" requirement.
- **Why put multiple classes in one file?** To keep the project small
  and easy to navigate for a course assignment. Each class still has
  its own single responsibility; they're just grouped by how closely
  related they are (all question types together, prompt-building and
  prompt-reading together).
- **Why did you drop regex from the parser?** Regex is powerful but
  hard to explain line-by-line in a viva. A plain `startsWith` check is
  just as effective for this fixed, predictable format and is much
  easier to defend under questioning.
