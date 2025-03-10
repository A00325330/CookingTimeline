/**
 * ✅ Renders ingredient list as HTML
 */
import { deleteIngredientFromTemp, updateIngredientInTemp } from "./api.js";

export function renderIngredients(ingredients, allowEdit = false) {
    if (!ingredients || ingredients.length === 0) return "<p>No ingredients yet.</p>";

    return `<ul>
        ${ingredients.map(ing => `
            <li>
                <strong>${ing.name}</strong> - ${ing.cookingTime} mins (${ing.cookingMethod})
                ${allowEdit ? `
                    <button class="btn btn-sm btn-warning edit-ingredient" data-id="${ing.id}">Edit</button>
                    <button class="btn btn-sm btn-danger delete-ingredient" data-id="${ing.id}">Delete</button>
                ` : ''}
            </li>
        `).join("")}
    </ul>`;
}

