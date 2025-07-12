// Game state
const gameState = {
    board: [],
    currentPlayer: null, // 'RED' oder 'BLUE'
    selectedPiece: null,
    legalMoves: [],
    gameOver: false,
    winner: null,
    winReason: null,
    isComputerPlayer: false, // Flag für Bot-Spieler
    apiUrl: 'http://localhost:8080/api' // Backend API URL
};

// Constants
const ROWS = 7;
const COLS = 7;
const PLAYER = {
    RED: 'RED',
    BLUE: 'BLUE'
};

// DOM elements
const boardElement = document.querySelector('.game-board');
const fenInput = document.getElementById('fen-input');
const loadFenButton = document.getElementById('load-fen');
const statusElement = document.createElement('div');
statusElement.className = 'game-status';
document.querySelector('.container').insertBefore(statusElement, document.querySelector('.game-rules'));

// Initialize the game
function initGame() {
    createBoard();

    // Add event listener for FEN loading
    loadFenButton.addEventListener('click', () => {
        const fen = fenInput.value.trim();
        if (fen) {
            loadFEN(fen);
            updateGameStatus();

            // Falls Rot am Zug ist, KI-Zug ausführen
            if (gameState.currentPlayer === PLAYER.RED && !gameState.gameOver) {
                setTimeout(makeComputerMove, 500);
            }
        }
    });

    // Add event listener for "New Game" button
    document.getElementById('new-game').addEventListener('click', () => {
        // Lade das Brett mit dem Ausgangs-FEN neu
        const defaultFen = "r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 b";
        fenInput.value = defaultFen;
        loadFEN(defaultFen);

        // Setze Spielzustand zurück
        gameState.gameOver = false;
        gameState.winner = null;
        gameState.winReason = null;

        // Aktualisiere UI
        updateGameStatus();

        // Verstecke Game-Over-Container
        document.getElementById('game-over-container').classList.add('hidden');
    });

    // Load initial board
    loadFEN(fenInput.value.trim());
    updateGameStatus();

    // Falls Rot am Zug ist, KI-Zug ausführen
    if (gameState.currentPlayer === PLAYER.RED && !gameState.gameOver) {
        setTimeout(makeComputerMove, 500);
    }
}

// Create the board in the DOM
function createBoard() {
    boardElement.innerHTML = '';
    gameState.board = [];

    for (let row = 0; row < ROWS; row++) {
        gameState.board[row] = [];

        for (let col = 0; col < COLS; col++) {
            const cell = document.createElement('div');
            cell.className = 'cell';
            cell.dataset.row = row;
            cell.dataset.col = col;

            // Add field coloring
            if (row === 0) {
                cell.classList.add('red-field');
            } else if (row === ROWS - 1) {
                cell.classList.add('blue-field');
            } else {
                cell.classList.add('neutral-field');
            }

            // Add coordinate label
            const coordinate = document.createElement('div');
            coordinate.className = 'cell-coordinate';
            const colLabel = String.fromCharCode(65 + col); // A, B, C...
            const rowLabel = ROWS - row; // 7, 6, 5...
            coordinate.textContent = `${colLabel}${rowLabel}`;
            cell.appendChild(coordinate);

            // Add click event for piece selection and movement
            cell.addEventListener('click', handleCellClick);

            boardElement.appendChild(cell);
            gameState.board[row][col] = null;
        }
    }
}

// Parse FEN notation and update board - uses only the board representation,
// the actual game logic comes from the backend
function loadFEN(fen) {
    // Clear the board
    for (let row = 0; row < ROWS; row++) {
        for (let col = 0; col < COLS; col++) {
            gameState.board[row][col] = null;
            clearCell(row, col);
        }
    }

    try {
        // Split the FEN string to get board state and current player
        const parts = fen.split(' ');
        if (parts.length < 2) {
            console.error("Ungültiger FEN-String: Spieler am Zug fehlt");
            return;
        }

        const boardParts = parts[0].split('/');
        gameState.currentPlayer = parts[1] === 'r' ? PLAYER.RED : PLAYER.BLUE;

        // Begrenze auf maximal 7 Zeilen (7x7 Brett)
        const maxRows = Math.min(ROWS, boardParts.length);

        // Process board state
        for (let row = 0; row < maxRows; row++) {
            let col = 0;
            const rowStr = boardParts[row];

            for (let i = 0; i < rowStr.length && col < COLS;) {
                const currentChar = rowStr[i];

                if (/\d/.test(currentChar)) {
                    let numStr = '';
                    while (i < rowStr.length && /\d/.test(rowStr[i])) {
                        numStr += rowStr[i];
                        i++;
                    }
                    const emptyCount = parseInt(numStr);

                    const fieldsToSkip = Math.min(emptyCount, COLS - col);
                    for (let j = 0; j < fieldsToSkip; j++) {
                        gameState.board[row][col] = null;
                        col++;
                    }
                } else if (currentChar === 'r' || currentChar === 'R' || currentChar === 'b' || currentChar === 'B') {
                    // Create piece object
                    const player = currentChar.toLowerCase() === 'r' ? PLAYER.RED : PLAYER.BLUE;
                    let piece = { player };

                    // Check if it's a Guard (RG or BG)
                    if (i + 1 < rowStr.length && rowStr[i + 1] === 'G') {
                        piece.isGuard = true;
                        piece.height = 1; // Guard height is always 1
                        i += 2;
                    } else {
                        // It's a tower, check its height
                        piece.isGuard = false;
                        i++;

                        if (i < rowStr.length && /\d/.test(rowStr[i])) {
                            const height = parseInt(rowStr[i]);
                            if (height >= 1 && height <= 9) {
                                piece.height = height;
                            } else {
                                piece.height = 1;
                            }
                            i++;
                        } else {
                            piece.height = 1;
                        }
                    }

                    if (col < COLS) {
                        gameState.board[row][col] = piece;
                        placePieceElement(row, col, piece);
                        col++;
                    }
                } else {
                    // Skip other characters
                    i++;
                }
            }
        }
    } catch (error) {
        console.error("Fehler beim Parsen des FEN-Strings:", error);
    }

    // Clear selection
    gameState.selectedPiece = null;
    gameState.legalMoves = [];
    clearHighlights();
}

// Clear a cell
function clearCell(row, col) {
    const cellIndex = row * COLS + col;
    const cell = boardElement.children[cellIndex];

    // Keep only the coordinate label
    const coordinate = cell.querySelector('.cell-coordinate');
    cell.innerHTML = '';
    cell.appendChild(coordinate);
}

// Place a piece element on the board
function placePieceElement(row, col, piece) {
    const cellIndex = row * COLS + col;
    const cell = boardElement.children[cellIndex];

    // Clear existing content but keep coordinate
    clearCell(row, col);

    // Create and add the piece
    if (piece) {
        const pieceElement = document.createElement('div');

        if (piece.isGuard) {
            // Guard piece
            pieceElement.className = `piece ${piece.player === PLAYER.RED ? 'red-guard' : 'blue-guard'}`;
            pieceElement.textContent = 'G';
        } else {
            // Tower piece
            pieceElement.className = `piece ${piece.player === PLAYER.RED ? 'red-piece' : 'blue-piece'}`;
            pieceElement.textContent = piece.height;
        }

        cell.appendChild(pieceElement);
    }
}

// Generate a FEN string from the current board state
function generateFEN() {
    let fen = '';

    for (let row = 0; row < ROWS; row++) {
        let emptyCount = 0;

        for (let col = 0; col < COLS; col++) {
            const piece = gameState.board[row][col];

            if (piece === null) {
                emptyCount++;
            } else {
                // Add any pending empty squares
                if (emptyCount > 0) {
                    fen += emptyCount;
                    emptyCount = 0;
                }

                // Add the piece
                let pieceChar = piece.player === PLAYER.RED ? (piece.isGuard ? 'R' : 'r') : (piece.isGuard ? 'B' : 'b');

                if (piece.isGuard) {
                    fen += pieceChar + 'G';
                } else {
                    // Begrenzte Turmhöhe auf 1-9
                    const validHeight = Math.max(1, Math.min(9, piece.height));
                    fen += pieceChar + validHeight;
                }
            }
        }

        // Add any pending empty squares at the end of the row
        if (emptyCount > 0) {
            fen += emptyCount;
        }

        // Add row separator if not the last row
        if (row < ROWS - 1) {
            fen += '/';
        }
    }

    // Add current player
    fen += ' ' + (gameState.currentPlayer === PLAYER.RED ? 'r' : 'b');

    return fen;
}

// Fetch legal moves from Java backend
async function fetchLegalMoves(row, col) {
    try {
        const fen = generateFEN();
        const position = String.fromCharCode(65 + col) + (7 - row);

        console.log(`DEBUG - Anfrage legale Züge für Position ${position} mit FEN: ${fen}`);

        // Anfrage an das Backend
        const response = await fetch(`${gameState.apiUrl}/moves?fen=${encodeURIComponent(fen)}&position=${position}`);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Fehler bei der Anfrage legaler Züge: ${errorText}`);
            throw new Error('Fehler beim Abrufen der möglichen Züge');
        }

        const responseText = await response.text();
        const data = JSON.parse(responseText);

        if (!data.moves || !Array.isArray(data.moves)) {
            console.error("Ungültiges Format der Züge vom Backend");
            return [];
        }

        console.log(`Erhaltene legale Züge: ${JSON.stringify(data.moves)}`);

        // Konvertiere das Backend-Format in unser Frontend-Format
        return data.moves.map(moveStr => {
            // Format vom Backend: "B3-C3-1"
            const parts = moveStr.split('-');
            if (parts.length < 3) {
                console.error(`Ungültiges Zugformat: ${moveStr}`);
                return null;
            }

            const [from, to, steps] = parts;

            // Konvertiere Koordinaten für die Darstellung im Frontend
            const toCol = to.charCodeAt(0) - 65;  // A -> 0, B -> 1, etc.
            const toRow = 7 - parseInt(to.substring(1));  // 1 -> 6, 7 -> 0

            return {
                toRow,
                toCol,
                steps: parseInt(steps),
                originalTo: to  // Originalwert vom Backend
            };
        }).filter(move => move !== null); // Filtere ungültige Züge
    } catch (error) {
        console.error('Fehler bei der Kommunikation mit dem Backend:', error);
        return [];
    }
}

// Führe einen Zug direkt mit den Backend-Koordinaten aus
async function makeMove(fromPosition, toPosition) {
    try {
        const fen = generateFEN();

        console.log(`Sende Zug an Backend: von ${fromPosition} nach ${toPosition} mit FEN: ${fen}`);

        // Anfrage an das Backend
        const response = await fetch(`${gameState.apiUrl}/move`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fen: fen,
                from: fromPosition,
                to: toPosition
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Fehler beim Ausführen des Zuges: ${errorText}`);
            throw new Error(`Fehler beim Ausführen des Zuges: ${response.status} ${response.statusText}`);
        }

        // Fix malformed JSON from backend - add quotes around move value
        const responseText = await response.text();
        const fixedJson = responseText.replace(/"move":([^,"{}]+)/, '"move":"$1"');
        const data = JSON.parse(fixedJson);

        console.log("Backend-Antwort:", data);

        // Markiere den Zug visuell
        highlightLastMove(fromPosition, toPosition);

        // Check if the game is over based on backend response
        if (data.gameOver) {
            gameState.gameOver = true;
            gameState.winner = data.winner === "red" ? PLAYER.RED : PLAYER.BLUE;
            gameState.winReason = data.winReason || "Spiel beendet";
            console.log("Spiel beendet! Gewinner:", gameState.winner, "Grund:", gameState.winReason);
        }

        // Lade das neue Brett vom Backend
        loadFEN(data.newFen);
        return true;
    } catch (error) {
        console.error('Backend-Fehler:', error);
        return false;
    }
}

// Handle cell click for piece selection and movement
async function handleCellClick(event) {
    if (gameState.gameOver || gameState.isComputerPlayer) return;

    const cell = event.currentTarget;
    const row = parseInt(cell.dataset.row);
    const col = parseInt(cell.dataset.col);
    const piece = gameState.board[row][col];

    // Erlaube nur Interaktion mit blauen Figuren (Spieler)
    if (gameState.currentPlayer !== PLAYER.BLUE) return;

    // Clear previous highlights
    clearHighlights();

    // If no piece is selected and there's a piece of the current player, select it
    if (gameState.selectedPiece === null && piece && piece.player === gameState.currentPlayer) {
        gameState.selectedPiece = { row, col, piece };
        cell.classList.add('selected');

        // Lade mögliche Züge vom Backend
        try {
            gameState.legalMoves = await fetchLegalMoves(row, col);
            // Wenn keine legalen Züge zurückgegeben werden, deselektiere die Figur
            if (gameState.legalMoves.length === 0) {
                console.log("Keine legalen Züge für diese Figur verfügbar");
                gameState.selectedPiece = null;
                cell.classList.remove('selected');
                return;
            }
            highlightLegalMoves();
        } catch (error) {
            console.error("Fehler beim Laden der legalen Züge:", error);
            gameState.selectedPiece = null;
            return;
        }
    }
    // If a piece is already selected
    else if (gameState.selectedPiece !== null) {
        const fromRow = gameState.selectedPiece.row;
        const fromCol = gameState.selectedPiece.col;

        // Check if the clicked cell is a legal move according to the backend
        const move = gameState.legalMoves.find(m => m.toRow === row && m.toCol === col);

        if (move) {
            // Verwende die originalen Koordinaten vom Backend für den Zug
            const fromPosition = String.fromCharCode(65 + fromCol) + (7 - fromRow);

            // Versuche den Zug über das Backend auszuführen
            const backendMoveSuccessful = await makeMove(fromPosition, move.originalTo);

            if (!backendMoveSuccessful) {
                console.error("Backend-Zug fehlgeschlagen");
                // Deselektiere die Figur bei Fehler
                gameState.selectedPiece = null;
                gameState.legalMoves = [];
                return;
            }

            // Reset selection
            gameState.selectedPiece = null;
            gameState.legalMoves = [];

            // Update game status
            updateGameStatus();

            // KI-Zug nach kurzer Verzögerung ausführen
            if (!gameState.gameOver) {
                gameState.isComputerPlayer = true;
                statusElement.textContent = "Rot (Computer) überlegt...";
                setTimeout(() => {
                    makeComputerMove();
                    gameState.isComputerPlayer = false;
                }, 700);
            }
        } else if (piece && piece.player === gameState.currentPlayer) {
            // Select a new piece of the same player
            gameState.selectedPiece = { row, col, piece };
            cell.classList.add('selected');

            // Lade mögliche Züge vom Backend
            try {
                gameState.legalMoves = await fetchLegalMoves(row, col);
                if (gameState.legalMoves.length === 0) {
                    console.log("Keine legalen Züge für diese Figur verfügbar");
                    gameState.selectedPiece = null;
                    cell.classList.remove('selected');
                    return;
                }
                highlightLegalMoves();
            } catch (error) {
                console.error("Fehler beim Laden der legalen Züge:", error);
                gameState.selectedPiece = null;
                return;
            }
        } else {
            // Deselect if clicking elsewhere
            gameState.selectedPiece = null;
        }
    }
}

// Make a computer move via backend AI
async function makeComputerMove() {
    if (gameState.gameOver || gameState.currentPlayer !== PLAYER.RED) return;

    try {
        // Versuche einen KI-Zug vom Backend zu bekommen
        const fen = generateFEN();
        console.log(`KI-Zug anfordern mit FEN: ${fen}`);

        const response = await fetch(`${gameState.apiUrl}/ai-move?fen=${encodeURIComponent(fen)}`);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Fehler beim KI-Zug: ${errorText}`);
            throw new Error('Fehler beim Abrufen des KI-Zuges');
        }

        // Fix malformed JSON from backend - add quotes around move value
        const responseText = await response.text();
        const fixedJson = responseText.replace(/"move":([^,"{}]+)/, '"move":"$1"');
        const data = JSON.parse(fixedJson);

        console.log("KI-Zug-Antwort:", data);

        // Check if the game is over based on backend response
        if (data.gameOver) {
            gameState.gameOver = true;
            gameState.winner = data.winner === "red" ? PLAYER.RED : PLAYER.BLUE;
            gameState.winReason = data.winReason || "Spiel beendet";
            console.log("Spiel beendet! Gewinner:", gameState.winner, "Grund:", gameState.winReason);
        }

        // Lade das neue Brett vom Backend
        loadFEN(data.newFen);

        // Wechsle den Spieler
        gameState.currentPlayer = PLAYER.BLUE;

        // Update game status
        updateGameStatus();

        // Update FEN
        fenInput.value = data.newFen;

        // Kurz markieren, welcher Zug gemacht wurde
        const from = data.move.split('-')[0];
        const to = data.move.split('-')[1];

        highlightLastMove(from, to);

        return;
    } catch (error) {
        console.error('Fehler beim Backend-KI-Zug:', error);
        statusElement.textContent = "Fehler beim KI-Zug. Bitte versuche es erneut.";

        // Bei Fehlern bleibt das Spiel stehen und der Spieler muss einen neuen Zug machen
        // oder die Seite neu laden
        gameState.isComputerPlayer = false;
    }
}

// Highlight legal moves
function highlightLegalMoves() {
    for (const move of gameState.legalMoves) {
        const cellIndex = move.toRow * COLS + move.toCol;
        const cell = boardElement.children[cellIndex];
        cell.classList.add('legal-move');
    }
}

// Clear all highlights
function clearHighlights() {
    const cells = boardElement.querySelectorAll('.cell');
    cells.forEach(cell => {
        cell.classList.remove('selected');
        cell.classList.remove('legal-move');
        cell.classList.remove('highlighted');
    });
}

// Update game status display
function updateGameStatus() {
    if (gameState.gameOver) {
        // Zeige speziellen Game-Over-Container an
        const gameOverContainer = document.getElementById('game-over-container');
        const winnerElement = document.getElementById('game-over-winner');
        const reasonElement = document.getElementById('game-over-reason');

        // Setze Gewinner-Text
        const winnerText = gameState.winner === PLAYER.RED ? 'Rot (Computer)' : 'Blau (Spieler)';
        winnerElement.textContent = `${winnerText} hat gewonnen!`;

        // Grund für den Sieg kommt vom Backend
        let winReason = gameState.winReason || "Spiel beendet durch Backend-Logik";
        reasonElement.textContent = `Grund: ${winReason}`;

        // Zeige den Game-Over-Container
        gameOverContainer.classList.remove('hidden');

        // Deaktiviere Spielinteraktionen
        boardElement.classList.add('disabled');

        // Status-Anzeige aktualisieren
        statusElement.textContent = `SPIEL BEENDET! ${winnerText} hat gewonnen!`;
        statusElement.classList.add('game-over');
    } else {
        // Verstecke Game-Over-Container
        document.getElementById('game-over-container').classList.add('hidden');

        // Normaler Spielstatus
        statusElement.classList.remove('game-over');
        boardElement.classList.remove('disabled');
        statusElement.textContent = `Aktueller Spieler: ${gameState.currentPlayer === PLAYER.RED ? 'Rot (Computer)' : 'Blau (Spieler)'}`;
    }
}

// Markiere den letzten Zug visuell
function highlightLastMove(from, to) {
    const fromCol = from.charCodeAt(0) - 65;
    const fromRow = 7 - parseInt(from.substring(1));
    const toCol = to.charCodeAt(0) - 65;
    const toRow = 7 - parseInt(to.substring(1));

    const fromCell = boardElement.children[fromRow * COLS + fromCol];
    const toCell = boardElement.children[toRow * COLS + toCol];

    fromCell.classList.add('highlighted');
    toCell.classList.add('highlighted');

    setTimeout(() => {
        fromCell.classList.remove('highlighted');
        toCell.classList.remove('highlighted');
    }, 800);
}

// Initialize the game when the page loads
window.addEventListener('DOMContentLoaded', initGame); 