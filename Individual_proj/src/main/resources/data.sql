-- Insert users
INSERT INTO users (id, email, password) VALUES 
(1, 'admin@example.com', '$2a$10$HZRxlurLYB4WOziqN57xY.KBu3l0sNpRTn/tR27H6RBKLW0zSAF6W'),
(2, 'user@example.com', '$2a$10$HZRxlurLYB4WOziqN57xY.KBu3l0sNpRTn/tR27H6RBKLW0zSAF6W');

-- Assign roles to users
INSERT INTO user_roles (user_id, role) VALUES 
(1, 'ADMIN'),
(2, 'USER');

-- Insert a recipe (Curry Recipe)
INSERT INTO recipes (id, name, description, cooking_time, user_id, cooking_method, visibility, start_time, finish_time, is_temporary) 
VALUES (1, 'Classic Chicken Curry', 'A delicious homemade chicken curry.', 45, 2, 'BOIL', 'PUBLIC', NOW(), NOW() + INTERVAL 45 MINUTE, FALSE);

INSERT INTO recipe_ingredients (recipe_id, name, cooking_time, cooking_method) VALUES 
(1, 'Chicken', 45, 'Boil'),
(1, 'Onions', 10, 'Sauté'),
(1, 'Garlic', 5, 'Sauté'),
(1, 'Ginger', 5, 'Sauté'),
(1, 'Tomatoes', 8, 'Simmer'),
(1, 'Curry Powder', 2, 'Mix'),
(1, 'Coconut Milk', 15, 'Simmer'),
(1, 'Salt', 1, 'Mix'),
(1, 'Pepper', 1, 'Mix');



-- Insert recipe steps
INSERT INTO recipe_steps (recipe_id, steps) VALUES 
(1, 'Heat oil in a pan and sauté onions, garlic, and ginger.'),
(1, 'Add chicken and cook until browned.'),
(1, 'Add chopped tomatoes and curry powder, cook for 10 minutes.'),
(1, 'Pour in coconut milk and let simmer for 20 minutes.'),
(1, 'Season with salt and pepper.'),
(1, 'Serve hot with rice or naan.');
