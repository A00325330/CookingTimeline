-- Insert users
INSERT INTO users (id, email, password) VALUES 
(1,'admin@example.com','$2a$10$4oTjrMo.u2vcMr9zwQJi8O5Bci2HCz4Em6LnJZ2uN5G/G0wEOabFW'),
(2,'user@example.com', '$2a$10$4oTjrMo.u2vcMr9zwQJi8O5Bci2HCz4Em6LnJZ2uN5G/G0wEOabFW');
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;

-- Assign roles
INSERT INTO user_roles (user_id, role) VALUES 
(1, 'ADMIN'),
(2, 'USER');

-- Insert a public recipe by user@example.com
INSERT INTO recipes (id, name, description, cooking_time, user_id, cooking_method, visibility, start_time, finish_time, is_temporary)
VALUES (1, 'Classic Chicken Curry', 'A delicious homemade chicken curry.', 45, 2, 'BOIL', 'PUBLIC', NOW(), DATEADD('MINUTE', 45, NOW()), FALSE);
ALTER TABLE recipes ALTER COLUMN id RESTART WITH 2;




-- Ingredients
INSERT INTO recipe_ingredients (recipe_id, name, cooking_time, cooking_method) VALUES 
(1, 'Chicken', 45, 'Boil'),
(1, 'Onions', 10, 'Sauté'),
(1, 'Garlic', 5, 'Sauté'),
(1, 'Tomatoes', 8, 'Simmer');

-- Steps
INSERT INTO recipe_steps (recipe_id, steps) VALUES 
(1, 'Sauté onions and garlic.'),
(1, 'Add chicken and brown.'),
(1, 'Add tomatoes and simmer.'),
(1, 'Serve hot.');
