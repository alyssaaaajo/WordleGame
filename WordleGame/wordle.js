const WORD_LIST = ["apple", "brave", "charm", "daisy", "eagle"];
const SECRET_WORD = WORD_LIST[Math.floor(Math.random() * WORD_LIST.length)];
const MAX_ATTEMPTS = 6;

let attemptsLeft = MAX_ATTEMPTS;
let attempts = [];

document.addEventListener("DOMContentLoaded", () => {
    createGrid();
    updateAttemptsLeft();
});

function createGrid() {
    const grid = document.getElementById("grid");
    for (let i = 0; i < MAX_ATTEMPTS; i++) {
        const row = document.createElement("div");
        row.classList.add("grid-row");
        for (let j = 0; j < 5; j++) {
            const cell = document.createElement("div");
            cell.classList.add("grid-cell");
            row.appendChild(cell);
        }
        grid.appendChild(row);
    }
}

function updateAttemptsLeft() {
    document.getElementById("attempts-count").innerText = attemptsLeft;
}

function submitGuess() {
    const guessInput = document.getElementById("guess");
    const guess = guessInput.value.toLowerCase();
    if (guess.length !== 5 || !WORD_LIST.includes(guess)) {
        alert("Please enter a valid 5-letter word.");
        return;
    }

    if (attemptsLeft > 0) {
        attempts.push(guess);
        attemptsLeft--;
        updateAttemptsLeft();
        displayGuess(guess);
        if (guess === SECRET_WORD) {
            alert("🎉 Congratulations! You guessed the word!");
        } else if (attemptsLeft === 0) {
            alert(`😢 Game Over! The correct word was: ${SECRET_WORD}`);
        }
    }
    guessInput.value = "";
}

function displayGuess(guess) {
    const row = document.querySelectorAll(".grid-row")[MAX_ATTEMPTS - attemptsLeft - 1];
    const cells = row.querySelectorAll(".grid-cell");
    
    for (let i = 0; i < 5; i++) {
        const letter = guess[i];
        const cell = cells[i];
        cell.innerText = letter;

        if (letter === SECRET_WORD[i]) {
            cell.style.backgroundColor = "green";
        } else if (SECRET_WORD.includes(letter)) {
            cell.style.backgroundColor = "yellow";
        } else {
            cell.style.backgroundColor = "gray";
        }
    }
}
