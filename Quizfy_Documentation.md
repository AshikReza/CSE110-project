# 📚 Quizfy — Full Project Documentation
### CSE110 Console Flashcard Maker

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [How to Run the Project](#2-how-to-run-the-project)
3. [Project Structure — Files at a Glance](#3-project-structure--files-at-a-glance)
4. [Architecture: How All Files Connect](#4-architecture-how-all-files-connect)
5. [File-by-File Deep Dive](#5-file-by-file-deep-dive)
   - [Main.java](#51-mainjava--the-entry-point--ui-controller)
   - [Question.java](#52-questionjava--the-data-model-layer)
   - [Deck.java](#53-deckjava--the-collection-manager)
   - [PromptTools.java](#54-prompttoolsjava--the-ai-bridge)
   - [Reviewable.java](#55-reviewablejava--the-interface-contract)
   - [InvalidQuestionTextException.java](#56-invalidquestiontextexceptionjava)
   - [InvalidPromptFormatException.java](#57-invalidpromptformatexceptionjava)
   - [EmptyDeckException.java](#58-emptydeckexceptionjava)
6. [Step-by-Step Program Flow](#6-step-by-step-program-flow)
7. [Feature Walkthrough](#7-feature-walkthrough)
8. [OOP Concepts Demonstrated](#8-oop-concepts-demonstrated)
9. [Exception Handling Map](#9-exception-handling-map)
10. [Class Relationship Diagram (Text)](#10-class-relationship-diagram-text)
11. [Viva / Presentation Talking Points](#11-viva--presentation-talking-points)

---

## 1. Project Overview

**Quizfy** is a **console-based, AI-assisted flashcard application** built in Java for a CSE110 (Object-Oriented Programming) course project.

### What it does
The app lets a student:
1. **Generate a prompt** — It outputs a formatted text block that the user copies and pastes into any AI chatbot (ChatGPT, Gemini, Claude).
2. **Import the AI's reply** — The user pastes the chatbot's response back into the terminal; the app parses it and converts it into structured flashcard objects.
3. **Study, quiz, search, edit, delete, bookmark, and shuffle** those flashcards interactively from a numbered menu.
4. **Save and load** decks to/from binary `.dat` files on disk.

### Key design decision: **No AI API is called**
The project deliberately avoids any network/API call. The AI interaction is entirely manual — copy the prompt, paste the reply. This keeps the project lightweight (no dependencies, no tokens, no internet required) and easy to run anywhere with just a JDK.

### Tech stack
- **Language**: Java 17+
- **Libraries**: Standard Java only (`java.io`, `java.util`)
- **Build**: No build tools — plain `javac` + `java`
- **Storage**: Java Object Serialization (`.dat` files)

---

## 2. How to Run the Project

### Option A — Windows (double-click)
```
Double-click run.bat
```
The batch file:
1. `cd`s into the `src/` folder.
2. Compiles all necessary `.java` files with `javac`.
3. Runs `java Main` to start the app.

### Option B — Manual (any OS with JDK 17+)
```bash
cd "CSE110 project/src"
javac Main.java Deck.java Question.java PromptTools.java InvalidPromptFormatException.java EmptyDeckException.java InvalidQuestionTextException.java Reviewable.java
java Main
```

> [!NOTE]
> All `.class` files are compiled into the **same `src/` folder** (no `-d out` flag is used in `run.bat`). The `out/` directory in the repo already contains pre-compiled classes from a previous build.

---

## 3. Project Structure — Files at a Glance

```
CSE110 project/
├── run.bat                         ← Windows one-click compile & run
├── README.md                       ← Brief setup guide (original)
├── uml.png                         ← UML class diagram image
├── out/                            ← Pre-compiled .class files
└── src/                            ← All source code lives here
    ├── Main.java                   ← Entry point + entire console UI
    ├── Question.java               ← Abstract Question + 3 subclasses
    ├── Deck.java                   ← Flashcard collection + file I/O
    ├── PromptTools.java            ← PromptGenerator + Parser (two classes)
    ├── Reviewable.java             ← Interface with 3 abstract methods
    ├── InvalidQuestionTextException.java   ← Custom checked exception
    ├── InvalidPromptFormatException.java   ← Custom checked exception
    └── EmptyDeckException.java     ← Custom checked exception
```

| File | Classes Inside | Lines (approx.) | Role |
|---|---|---|---|
| `Main.java` | `Main` | 268 | Console UI, menu loop, all user interaction |
| `Question.java` | `Question`, `MCQQuestion`, `TrueFalseQuestion`, `FillBlankQuestion` | 156 | Data model — stores and evaluates all question types |
| `Deck.java` | `Deck` | 52 | Collection — holds all flashcards, save/load |
| `PromptTools.java` | `PromptGenerator`, `Parser` | 152 | AI integration — builds prompts, parses AI output |
| `Reviewable.java` | `Reviewable` *(interface)* | 20 | Contract — forces all question types to support display/check/show |
| `InvalidQuestionTextException.java` | `InvalidQuestionTextException` | 11 | Error for bad question text |
| `InvalidPromptFormatException.java` | `InvalidPromptFormatException` | 10 | Error for malformed AI output |
| `EmptyDeckException.java` | `EmptyDeckException` | 14 | Error for operations on an empty deck |

> [!NOTE]
> Java allows **multiple non-public classes in one `.java` file**. This is how 9 classes fit into 5 source files — e.g., `MCQQuestion`, `TrueFalseQuestion`, and `FillBlankQuestion` all live inside `Question.java`.

---

## 4. Architecture: How All Files Connect

```
┌─────────────────────────────────────────────────────────────────────┐
│                            Main.java                                │
│  (Entry point, menu loop, orchestrator of everything)               │
│                                                                     │
│  owns ──► Deck          calls ──► PromptGenerator                   │
│           Deck.add()             Parser.parse()                     │
│           Deck.search()                                             │
│           Deck.shuffle()                                            │
│           Deck.saveToFile()                                         │
│           Deck.loadFromFile()                                       │
└───────────────┬─────────────────────────┬───────────────────────────┘
                │                         │
                ▼                         ▼
        ┌──────────────┐         ┌──────────────────────┐
        │   Deck.java  │         │   PromptTools.java   │
        │              │         │                      │
        │ ArrayList    │         │  PromptGenerator     │
        │ <Question>   │         │  └─ generate()       │
        │              │         │                      │
        │ add / remove │         │  Parser              │
        │ search       │         │  └─ parse()          │
        │ shuffle      │         │     builds ──────────┼───►
        │ save / load  │         │                      │
        └──────┬───────┘         └──────────────────────┘
               │ holds many
               ▼
        ┌──────────────────────────────────────────────────┐
        │              Question.java (abstract)            │
        │   implements Reviewable, Serializable            │
        │                                                  │
        │   display()     ← abstract                       │
        │   checkAnswer() ← abstract                       │
        │   showAnswer()  ← abstract                       │
        │   getQuestionText() / setQuestionText()          │
        │   getExplanation()                               │
        │   isBookmarked() / setBookmarked()               │
        └─────────┬─────────────┬──────────────┬──────────┘
                  │             │              │
           extends│      extends│       extends│
                  ▼             ▼              ▼
          ┌──────────┐  ┌──────────────┐  ┌──────────────┐
          │MCQQuestion│  │TrueFalseQues-│  │FillBlankQues-│
          │          │  │tion          │  │tion          │
          │ options  │  │              │  │              │
          │ A,B,C,D  │  │correctAnswer │  │correctAnswer │
          │ correct  │  │(boolean)     │  │(String)      │
          │ Option   │  │              │  │              │
          └──────────┘  └──────────────┘  └──────────────┘

        ┌──────────────────────────────────────────────────┐
        │           Reviewable.java (interface)            │
        │                                                  │
        │   display()       : String                       │
        │   checkAnswer()   : boolean                      │
        │   showAnswer()    : String                       │
        └──────────────────────────────────────────────────┘
              ▲ implemented by Question

  Exceptions thrown throughout:
  ┌─────────────────────────────────┐
  │ InvalidQuestionTextException    │  ← thrown by Question constructor & setQuestionText()
  │ InvalidPromptFormatException    │  ← thrown by Parser.parse()
  │ EmptyDeckException              │  ← thrown by Main.requireNonEmptyDeck()
  └─────────────────────────────────┘
```

---

## 5. File-by-File Deep Dive

---

### 5.1 `Main.java` — The Entry Point & UI Controller

**Location**: [`src/Main.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/Main.java)  
**Class**: `Main` (public)  
**Lines**: 268

#### Purpose
This is the **only class with a `main()` method**. It is the program's entry point and acts as the **controller** — it owns the `Deck`, reads user input via a `Scanner`, and calls the appropriate methods on `Deck`, `PromptGenerator`, and `Parser`.

#### Fields
```java
private static Deck deck = new Deck("My Deck");  // the single deck in use
private static final Scanner sc = new Scanner(System.in);  // user input reader
```

#### Program Startup Sequence
```
main() is called
  └─► prints "Quizfy - Console Flashcard Maker" banner
  └─► enters while(running) loop
        └─► printMenu()         ← prints numbered options + deck size
        └─► reads user's choice (1-11 or 0 to quit)
        └─► routes to the correct private method via switch
```

#### Menu Options and the Methods They Call

| Option | Method Called | What It Does |
|---|---|---|
| `1` | `generatePrompt()` | Builds an AI prompt string and prints it |
| `2` | `importFlashcards()` | Reads pasted AI text, calls `Parser.parse()`, adds cards to deck |
| `3` | `studyFlashcards()` | Shows each card's question, waits for Enter, reveals answer |
| `4` | `startQuiz()` | Shows question, reads user answer, calls `q.checkAnswer()`, scores |
| `5` | `searchCards()` | Calls `deck.search(keyword)`, prints matches |
| `6` | `editCard()` | Lists cards, picks one by index, calls `q.setQuestionText()` |
| `7` | `deleteCard()` | Lists cards, picks one, calls `deck.remove(index)` |
| `8` | `bookmarkCard()` | Toggles `q.setBookmarked(!q.isBookmarked())` |
| `9` | *(inline)* | Calls `deck.shuffle()` |
| `10` | `saveDeck()` | Prompts for filename, calls `deck.saveToFile(path)` |
| `11` | `loadDeck()` | Prompts for filename, calls `Deck.loadFromFile(path)` |
| `0` | *(inline)* | Sets `running = false`, exits loop |

#### Key Helper: `pickCardIndex()`
Used by edit, delete, and bookmark. It:
1. Checks `requireNonEmptyDeck()` first.
2. Prints a numbered list of all cards.
3. Reads a number from the user.
4. Returns the **zero-based** index (subtracts 1 from the user's input).

#### Key Helper: `requireNonEmptyDeck()`
```java
private static void requireNonEmptyDeck() throws EmptyDeckException {
    if (deck.size() == 0) throw new EmptyDeckException();
}
```
Called before any operation that needs at least one card.

---

### 5.2 `Question.java` — The Data Model Layer

**Location**: [`src/Question.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/Question.java)  
**Classes**: `Question` (abstract, public), `MCQQuestion`, `TrueFalseQuestion`, `FillBlankQuestion`  
**Lines**: 156

#### Why one file for 4 classes?
Java allows multiple classes per `.java` file — only the `public` one must match the filename. The three subclasses are **package-private** (no `public` keyword), so they live alongside their parent class.

---

#### `Question` (abstract)

```java
public abstract class Question implements Serializable, Reviewable
```

**Fields** (all `protected` so subclasses can inherit):
```java
protected String questionText;   // the question prompt
protected String explanation;    // "Why" text shown after an answer
protected boolean bookmarked;    // ★ flag for flagging important cards
```

**Constructor — validation built-in**:
```java
public Question(String questionText, String explanation) throws InvalidQuestionTextException
```
- Throws `InvalidQuestionTextException` if `questionText` is `null` or blank.
- Sets `explanation` to `""` if `null` is passed.
- Sets `bookmarked = false` by default.

**Abstract methods** (must be overridden by each subclass):
```java
public abstract String display();          // formats the question for display
public abstract boolean checkAnswer(String userAnswer);  // grades the answer
public abstract String showAnswer();       // returns the correct answer as a String
```

**Concrete getters/setters** (shared by all subclasses):
- `getQuestionText()` / `setQuestionText()` — setter also validates (throws `InvalidQuestionTextException`)
- `getExplanation()`
- `isBookmarked()` / `setBookmarked(boolean)`

**`toString()`**:
```java
return (bookmarked ? "* " : "") + questionText;
```
Prepends `* ` if the card is bookmarked. Used when listing cards in menus.

---

#### `MCQQuestion` (extends Question)

Stores a 4-option multiple-choice question.

**Extra fields**:
```java
private String optionA, optionB, optionC, optionD;
private char correctOption;  // stores 'A', 'B', 'C', or 'D' (uppercase)
```

**Constructor**:
```java
MCQQuestion(String questionText, String explanation,
            String a, String b, String c, String d, char correct)
```
- Calls `super(questionText, explanation)` first.
- Converts `correct` to uppercase with `Character.toUpperCase()`.
- Validates that `correct` is one of `ABCD`, else throws `IllegalArgumentException`.

**`display()`** — returns:
```
What is polymorphism?
  A) Only inheritance
  B) One interface, many implementations
  C) A data structure
  D) A Java keyword
```

**`checkAnswer(String userAnswer)`**:
- Reads only the **first character** of the user's input.
- Case-insensitive comparison against `correctOption`.

**`showAnswer()`** — returns e.g. `B) One interface, many implementations`

---

#### `TrueFalseQuestion` (extends Question)

**Extra field**:
```java
private boolean correctAnswer;  // true or false
```

**`display()`** — appends `(Type True or False)` hint.

**`checkAnswer(String userAnswer)`**:
- Reads the **first character** (`t`/`f`, case-insensitive).
- Returns `correctAnswer` if starts with `t`, `!correctAnswer` if starts with `f`.
- Throws `IllegalArgumentException` if neither.

**`showAnswer()`** — returns `"True"` or `"False"`.

---

#### `FillBlankQuestion` (extends Question)

**Extra field**:
```java
private String correctAnswer;  // the exact expected phrase
```

**Constructor**: Also validates that `correctAnswer` is not blank (throws `InvalidQuestionTextException`).

**`checkAnswer(String userAnswer)`**:
```java
return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
```
Case-insensitive, whitespace-trimmed exact match.

**`display()`** — appends `(Fill in the blank)` hint.

---

### 5.3 `Deck.java` — The Collection Manager

**Location**: [`src/Deck.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/Deck.java)  
**Class**: `Deck` (public, implements `Serializable`)  
**Lines**: 52

#### Purpose
`Deck` is the **data store**. It wraps an `ArrayList<Question>` and adds higher-level operations on top: search, shuffle, save, and load.

#### Fields
```java
private String name;                    // e.g. "My Deck"
private ArrayList<Question> questions;  // the list of all flashcards
```

#### Methods

| Method | What It Does |
|---|---|
| `add(Question q)` | Appends a card to the list |
| `remove(int index)` | Removes the card at position `index` |
| `search(String keyword)` | Case-insensitive search in `questionText`; returns matching cards |
| `shuffle()` | Calls `Collections.shuffle(questions)` — randomizes order |
| `getQuestions()` | Returns the full `ArrayList<Question>` |
| `getName()` | Returns the deck name |
| `size()` | Returns `questions.size()` |
| `saveToFile(String filePath)` | Serializes **the whole `Deck` object** to a binary file using `ObjectOutputStream` |
| `loadFromFile(String filePath)` | Deserializes and returns a `Deck` from a binary file using `ObjectInputStream` |

#### How File Save/Load Works
Because `Deck implements Serializable` AND `Question implements Serializable`, Java's built-in serialization writes **the entire object graph** — including the deck name, all question objects, their type, their answers, their bookmark flags — into a single binary file.

```java
// Saving
ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
out.writeObject(this);  // "this" = the Deck, which drags all its Questions with it

// Loading
ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
return (Deck) in.readObject();  // cast back to Deck
```

---

### 5.4 `PromptTools.java` — The AI Bridge

**Location**: [`src/PromptTools.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/PromptTools.java)  
**Classes**: `PromptGenerator`, `Parser` (both package-private)  
**Lines**: 152

This file contains two classes that handle the **interface between the user and an external AI chatbot**.

---

#### `PromptGenerator`

```java
class PromptGenerator
```

**One static method**: `generate(String topic, int count, String difficulty, String types) : String`

It builds and returns a **multi-line String** that the user pastes into ChatGPT/Gemini/Claude. The prompt:
- Tells the AI what topic, how many questions, what difficulty.
- Specifies **exactly the output format** the `Parser` expects.

**Example output** (for topic "Java OOP", count 3, difficulty "Medium", types "MCQ"):
```
Generate 3 flashcards about Java OOP.

Difficulty: Medium
Question Types: MCQ

Return the output ONLY in this exact format, one block per question,
with each field on its own line, separated by a line containing only ---

Question: ...
Type: (MCQ | TRUE_FALSE | FILL_BLANK)
Option A: ...   (MCQ only)
...
Answer: ...
Explanation: ...
---
```

**Validations**:
- Throws `IllegalArgumentException` if `topic` is blank or `count <= 0`.

---

#### `Parser`

```java
class Parser
```

**One static method**: `parse(String rawText) : ArrayList<Question>` *(throws InvalidPromptFormatException)*

**Algorithm — step by step**:

```
Step 1: Split the whole AI response on "---" lines
        → produces a List of "blocks", one per question

Step 2: For each block:
    Step 2a: Skip if all lines in the block are blank
    Step 2b: Call buildQuestionFromLines(block)

Step 3: In buildQuestionFromLines():
    → Loop through each line of the block
    → Call findLabel(line) — checks if the line starts with a known label
      (e.g. "Question:", "Type:", "Option A:", "Answer:", "Explanation:")
    → If found, extract the value after the ":" and store in a HashMap

Step 4: Read from the HashMap:
    questionText = fields.get("QUESTION")
    type         = fields.get("TYPE")
    answer       = fields.get("ANSWER")
    explanation  = fields.get("EXPLANATION")

Step 5: Decide which Question subclass to build:
    if type contains "MCQ" or there are "OPTION A" fields → build MCQQuestion
    if type contains "TRUE" or "FALSE"                  → build TrueFalseQuestion
    else                                                → build FillBlankQuestion

Step 6: Return the ArrayList<Question> to Main, which adds them to the Deck
```

**Key design choice: no regular expressions**  
The parser uses only `String.startsWith()` checks — explicit, readable, and easy to explain in a viva.

---

### 5.5 `Reviewable.java` — The Interface Contract

**Location**: [`src/Reviewable.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/Reviewable.java)  
**Type**: Java `interface`  
**Lines**: 20

```java
public interface Reviewable {
    String display();
    boolean checkAnswer(String userAnswer);
    String showAnswer();
}
```

#### Purpose
Defines the **minimum contract** that any reviewable item must fulfill. Currently, only `Question` implements it.

#### Why use an interface?
- It allows `Main` to reference any question by the `Reviewable` type if needed, without caring about the concrete subclass.
- Demonstrates the **"program to an interface"** OOP principle.
- If the project were extended with, say, a `FlashCard` class that is not a `Question`, it could still be added to the review flow just by implementing `Reviewable`.

---

### 5.6 `InvalidQuestionTextException.java`

**Location**: [`src/InvalidQuestionTextException.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/InvalidQuestionTextException.java)

```java
public class InvalidQuestionTextException extends Exception
```

**Thrown when**:
- A `Question` is constructed with `null` or blank `questionText`.
- `setQuestionText()` is called with `null` or blank text (e.g., user tries to edit a card to be empty).
- A `FillBlankQuestion` is constructed with a `null` or blank `correctAnswer`.

**Where caught**:
- `Main.editCard()` — catches it, prints a message, returns gracefully.
- `Parser.buildQuestionFromLines()` — re-wraps it as `InvalidPromptFormatException`.

---

### 5.7 `InvalidPromptFormatException.java`

**Location**: [`src/InvalidPromptFormatException.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/InvalidPromptFormatException.java)

```java
public class InvalidPromptFormatException extends Exception
```

**Thrown when**:
- The pasted AI text is empty.
- A block is missing a `Question:` field.
- A block is missing an `Answer:` field.
- An MCQ block is missing one of the `Option A/B/C/D:` fields.
- The MCQ letter answer is not A, B, C, or D.

**Where caught**:
- `Main.importFlashcards()` — prints the message and returns to menu.

---

### 5.8 `EmptyDeckException.java`

**Location**: [`src/EmptyDeckException.java`](file:///c:/Users/Ashik/Downloads/CSE110%20project/src/EmptyDeckException.java)

```java
public class EmptyDeckException extends Exception
```

**Default message**: `"The deck is empty. Please import flashcards first."`

**Thrown when**:
- `Main.requireNonEmptyDeck()` is called and `deck.size() == 0`.

**Where caught**:
- `Main.studyFlashcards()`, `startQuiz()`, `editCard()`, `deleteCard()`, `bookmarkCard()`, `pickCardIndex()`.

---

## 6. Step-by-Step Program Flow

### Full typical session walkthrough

```
[User runs Main.java]
        │
        ▼
"====================================="
"   Quizfy - Console Flashcard Maker"
"====================================="
        │
        ▼
╔═══════════════ MENU LOOP ══════════════╗
║ Deck: My Deck (0 cards)               ║
║ 1. Generate AI Prompt                 ║
║ 2. Import Flashcards                  ║
║ ...                                   ║
║ 0. Exit                               ║
╚════════════════════════════════════════╝
        │
        │ user types "1" (Generate Prompt)
        ▼
generatePrompt():
  ┌─ asks: study topic? → "Java OOP"
  ├─ asks: number of questions? → "5"
  ├─ asks: difficulty? → "Medium"
  ├─ asks: question types? → "Multiple Choice, True/False"
  └─ calls PromptGenerator.generate("Java OOP", 5, "Medium", "Multiple Choice, True/False")
       └─► prints the big prompt block to the console
        │
        │ user copies the prompt → pastes it into ChatGPT → gets a reply → copies the reply
        │
        │ user types "2" (Import Flashcards)
        ▼
importFlashcards():
  ┌─ "Paste the AI's response below. Type END on its own line:"
  ├─ reads lines until "END" is typed
  ├─ calls Parser.parse(pastedText)
  │     └─ splits on "---" → builds MCQQuestion / TrueFalseQuestion / FillBlankQuestion objects
  └─ calls deck.add(q) for each parsed question
  └─► "Imported 5 flashcards."
        │
        │ user types "3" (Study)
        ▼
studyFlashcards():
  ┌─ loop i = 0..4
  │   ├─ prints "Card 1/5:"
  │   ├─ prints q.display()  ← polymorphism: correct subclass method runs
  │   ├─ waits for Enter
  │   ├─ prints "Answer: " + q.showAnswer()
  │   └─ prints "Why: " + q.getExplanation()   (if not blank)
  └─► done
        │
        │ user types "4" (Quiz)
        ▼
startQuiz():
  ┌─ loop through all questions
  │   ├─ prints q.display()
  │   ├─ reads user answer
  │   ├─ calls q.checkAnswer(userAnswer) ← polymorphism again
  │   │   returns true/false
  │   └─ prints "Correct!" or "Wrong. Correct answer: ..."
  └─► "Quiz finished! Score: 4/5"
        │
        │ user types "10" (Save)
        ▼
saveDeck():
  ┌─ asks: file name? → "myjava.dat"
  └─ calls deck.saveToFile("myjava.dat")
       └─► ObjectOutputStream serializes the entire Deck object to disk
        │
        │ user types "0" (Exit)
        ▼
"Goodbye!"
[Program ends]
```

---

## 7. Feature Walkthrough

### Feature 1: Generate AI Prompt (Option 1)
- `Main.generatePrompt()` collects: topic, count, difficulty, types.
- Calls `PromptGenerator.generate(...)`.
- Prints the result between separator lines.
- **User's job**: copy it, paste into ChatGPT/Gemini, copy the reply.

### Feature 2: Import Flashcards (Option 2)
- `Main.importFlashcards()` reads multi-line input until `"END"`.
- Calls `Parser.parse(sb.toString())`.
- Each parsed `Question` object is added to `deck` via `deck.add(q)`.

### Feature 3: Study Mode (Option 3)
- Shows cards one by one.
- After Enter is pressed, reveals the answer AND explanation.
- No scoring — pure passive review.

### Feature 4: Quiz Mode (Option 4)
- Asks the user to type their answer for each card.
- Uses **polymorphic** `q.checkAnswer(userAnswer)` — each question type checks differently.
- Shows a final score at the end.

### Feature 5: Search (Option 5)
- `deck.search(keyword)` does a case-insensitive substring search in all `questionText` fields.
- Prints matching cards using their `toString()` method.

### Feature 6: Edit (Option 6)
- User picks a card by number.
- Can replace its `questionText` only (type-specific fields like options/answers are not editable from the menu).
- `setQuestionText()` validates the new text.

### Feature 7: Delete (Option 7)
- User picks a card by number.
- `deck.remove(index)` removes it from the `ArrayList`.

### Feature 8: Bookmark (Option 8)
- Toggles `q.setBookmarked(!q.isBookmarked())`.
- Bookmarked cards show a `* ` prefix in all list views (via `toString()`).

### Feature 9: Shuffle (Option 9)
- Calls `Collections.shuffle(questions)` in-place on the deck's list.
- Changes the order cards appear in Study, Quiz, and all lists.

### Feature 10 & 11: Save / Load (Options 10, 11)
- **Save**: serializes the entire `Deck` (with all its `Question` objects) to a binary `.dat` file.
- **Load**: deserializes the file back into a `Deck` and replaces the current `deck` in `Main`.

---

## 8. OOP Concepts Demonstrated

| Concept | Where It Appears |
|---|---|
| **Abstract class** | `Question` — cannot be instantiated directly; forces subclasses to implement `display()`, `checkAnswer()`, `showAnswer()` |
| **Inheritance** | `MCQQuestion`, `TrueFalseQuestion`, `FillBlankQuestion` all `extend Question` |
| **Polymorphism** | `Main` calls `q.display()`, `q.checkAnswer()`, `q.showAnswer()` on a `Question` reference — the JVM dispatches to the correct subclass at runtime |
| **Interface** | `Reviewable` defines the 3-method contract; `Question implements Reviewable` |
| **Encapsulation** | Fields in `Question` and `Deck` are `private`/`protected`; only getters/setters are public |
| **Custom Exceptions** | 3 custom exception classes for specific error conditions |
| **Collections** | `ArrayList<Question>` in `Deck`; `HashMap<String,String>` in `Parser` |
| **File I/O + Serialization** | `Deck.saveToFile()` / `loadFromFile()` using `ObjectOutputStream`/`ObjectInputStream` |
| **Static methods** | `PromptGenerator.generate()`, `Parser.parse()`, `Deck.loadFromFile()` |
| **StringBuilder** | Used in `PromptGenerator.generate()` and `Main.importFlashcards()` for string building |

---

## 9. Exception Handling Map

```
Exception                        Thrown By                          Caught By
─────────────────────────────────────────────────────────────────────────────────────
InvalidQuestionTextException   Question constructor               Parser (re-wraps)
                               Question.setQuestionText()        Main.editCard()
                               FillBlankQuestion constructor     Parser (re-wraps)

InvalidPromptFormatException   Parser.parse()                    Main.importFlashcards()
                               Parser.buildQuestionFromLines()
                               Parser.require()

EmptyDeckException             Main.requireNonEmptyDeck()        Main.studyFlashcards()
                                                                 Main.startQuiz()
                                                                 Main.pickCardIndex()

IllegalArgumentException       MCQQuestion constructor            Main.startQuiz()
                               TrueFalseQuestion.checkAnswer()   Main.generatePrompt()
                               PromptGenerator.generate()

NumberFormatException          Main.generatePrompt()             Main.generatePrompt()
                               Main.pickCardIndex()              Main.pickCardIndex()

IOException                    Deck.saveToFile()                 Main.saveDeck()
                               Deck.loadFromFile()               Main.loadDeck()

ClassNotFoundException         Deck.loadFromFile()               Main.loadDeck()
```

---

## 10. Class Relationship Diagram (Text)

```
«interface»
Reviewable
  ├─ display(): String
  ├─ checkAnswer(String): boolean
  └─ showAnswer(): String
         ▲ implements
         │
Question (abstract)  implements Serializable
  ├─ questionText: String
  ├─ explanation: String
  ├─ bookmarked: boolean
  ├─ + display()          [abstract]
  ├─ + checkAnswer()      [abstract]
  ├─ + showAnswer()       [abstract]
  ├─ + getQuestionText() / setQuestionText()
  ├─ + getExplanation()
  ├─ + isBookmarked() / setBookmarked()
  └─ + toString()
         │ extends
    ┌────┴──────────────────────┐
    ▼                           ▼                           ▼
MCQQuestion             TrueFalseQuestion          FillBlankQuestion
  optionA..D: String      correctAnswer: boolean     correctAnswer: String
  correctOption: char

Deck  implements Serializable
  ├─ name: String
  ├─ questions: ArrayList<Question>
  ├─ + add / remove / search / shuffle
  ├─ + getQuestions / getName / size
  ├─ + saveToFile(path)
  └─ + loadFromFile(path): Deck  [static]

PromptGenerator
  └─ + generate(topic, count, difficulty, types): String  [static]

Parser
  ├─ LABELS: String[]  [static]
  ├─ + parse(rawText): ArrayList<Question>  [static, throws InvalidPromptFormatException]
  ├─ - addBlockIfNotEmpty()  [static]
  ├─ - buildQuestionFromLines()  [static]
  ├─ - findLabel()  [static]
  └─ - require()  [static]

Main
  ├─ deck: Deck  [static]
  ├─ sc: Scanner  [static]
  └─ main()  →  printMenu() → switch → generatePrompt / importFlashcards / studyFlashcards
                                       startQuiz / searchCards / editCard / deleteCard
                                       bookmarkCard / shuffle / saveDeck / loadDeck

Exceptions (all extend Exception):
  InvalidQuestionTextException
  InvalidPromptFormatException
  EmptyDeckException
```

---

## 11. Viva / Presentation Talking Points

> [!TIP]
> These are common questions instructors ask about CSE110 OOP projects. Prepare these answers for your showcase.

### Q: Why is `Question` abstract?
**A:** Because `Question` on its own doesn't know *how* to display itself, check an answer, or show the correct answer — those details differ for MCQ vs. True/False vs. Fill-in-the-blank. Making it `abstract` forces every subclass to supply its own version of those methods. There's no giant `if (type.equals("MCQ"))` anywhere in the code — the right logic runs automatically via polymorphism.

---

### Q: Where exactly is polymorphism used?
**A:** In `Main.studyFlashcards()` and `Main.startQuiz()`. The loop variable is typed `Question`, but when the code calls `q.display()` or `q.checkAnswer(userAnswer)`, Java automatically runs the correct override from `MCQQuestion`, `TrueFalseQuestion`, or `FillBlankQuestion` depending on what the object actually is at runtime. The `Main` class never has to check which type it is.

---

### Q: Why does the project not call any AI API?
**A:** Two reasons. First, it meets the "no external API" course requirement. Second, it makes the project dependency-free — it runs with just `javac` and `java`, no API keys, no internet, no rate limits. The AI is used as a human-in-the-loop tool: the app generates a formatted prompt, the user copy-pastes it into any chatbot, and pastes the reply back in.

---

### Q: Why are multiple classes in one file?
**A:** Java allows it as long as only one class is `public` and matches the filename. It keeps the project organized by *responsibility group* — all question types belong together in `Question.java`, both prompt-related utilities belong together in `PromptTools.java`. Each class still has a single, clear responsibility; they're just co-located in the same file for simplicity.

---

### Q: Why no regex in the parser?
**A:** Regular expressions are powerful but hard to explain line-by-line in a viva. A plain `line.startsWith("Question:")` check is just as effective for a fixed, predictable AI output format and is much easier to read, debug, and defend under questioning. The trade-off is that a very long answer that wraps to two lines will be cut off, but in practice AI chatbots keep these fields short.

---

### Q: How does file save/load work?
**A:** `Deck` implements `Serializable`, and so does `Question` (and all its subclasses, which inherit `Serializable` from the parent). When `deck.saveToFile("myjava.dat")` is called, Java's `ObjectOutputStream` converts the entire `Deck` object — including all its nested `Question` objects — into a binary byte stream and writes it to disk. `loadFromFile` does the reverse: `ObjectInputStream` reads the bytes back and reconstructs the full object graph in memory.

---

### Q: What is the `Reviewable` interface for?
**A:** It defines the behavioral contract for anything that can be reviewed in a study/quiz session: you must be able to display it, check an answer for it, and show the correct answer. `Question` implements this contract. The interface is not strictly required since `Main` uses `Question` references directly, but it demonstrates the "program to an interface" principle and makes the design extensible — a future `VideoCard` or `ImageCard` class could implement `Reviewable` without being a `Question`.

---

*Documentation generated for CSE110 Project — Quizfy Console Flashcard Maker*
