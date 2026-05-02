# /explain — Explain a File or Concept

Explains a project file or technical concept in clear terms, aimed at someone who is learning.

## Usage
- `/explain JwtAuthFilter`
- `/explain SecurityConfig`
- `/explain TokensService`
- `/explain refresh token`
- `/explain rebase`

## Step 1: Determine whether it is a project file or a general concept

**If it is a project file:**
Find it in `src/main/java/` and read it completely before responding.

**If it is a general concept** (JWT, rebase, BCrypt, etc.):
Answer from general knowledge, using concrete examples from this project where possible.

## Step 2: Structure of the explanation

Adapt the level to the junior developer working on this project. Follow this order:

### What is it? (1-2 sentences)
The shortest possible answer to "what is this for?".

### Why does it exist in this project?
The specific problem it solves. For a file: what would happen if it did not exist.

### How does it work? (the core)
Explain the flow step by step in simple language.
- Use real-world analogies when they help
- For Java files: explain the main methods one by one
- Point out the most important lines or sections with the line number

### How does it connect with the rest of the project?
What calls it, what it calls, what it depends on.

### What does it NOT do? (if applicable)
Boundaries or common misconceptions about its responsibility.

### Concrete example (if applicable)
A real flow in the project that uses this piece (e.g. "when the user logs in, this is what happens in this file...").

## Step 3: Offer to go deeper

At the end, offer to expand on any specific aspect.
