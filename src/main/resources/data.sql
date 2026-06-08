-- Insertar eventos con IDs fijos (no random) para poder referenciarlos
INSERT INTO events (id, name, description, event_date, location, capacity, created_by, created_at, status) VALUES
                                                                                                               ('ef025378-2904-4311-9eb6-57911bc6ffcc', 'Festival Estéreo Picnic 2025', 'El festival de música más grande de Colombia regresa con artistas internacionales y nacionales. Tres días de música en vivo, arte y cultura en el Parque Simón Bolívar.', '2025-09-12 16:00:00+00', 'Parque Simón Bolívar, Bogotá', 50000, 'admin', now(), 'ACTIVE'),
                                                                                                               ('9dcf76b9-ca44-4192-b274-dd2d8b0afa2f', 'Feria de las Flores - Noche de Salsa', 'Noche especial de salsa caleña en el marco de la Feria de las Flores. Los mejores salseros de Cali y Medellín se unen en una noche inolvidable.', '2025-08-05 20:00:00+00', 'Plaza Mayor, Medellín', 8000, 'admin', now(), 'ACTIVE'),
                                                                                                               ('5c2ee69b-6352-4af7-842d-6bfa30880013', 'Congreso Internacional de Tecnología VivaFuture', 'Dos días de conferencias, talleres y networking con líderes tech de Latinoamérica. Temas: IA, blockchain, ciberseguridad y desarrollo de software.', '2025-10-03 08:00:00+00', 'Centro de Convenciones, Cali', 2000, 'admin', now(), 'ACTIVE'),
                                                                                                               ('95051f33-3243-4c25-b43d-6fcee9dc150c', 'Noche de Cine Bajo las Estrellas', 'Proyección al aire libre de clásicos del cine latinoamericano. Trae tu cobija, disfruta de comida artesanal y vive una experiencia única en familia.', '2025-07-19 19:00:00+00', 'Parque del Café, Montenegro, Quindío', 1200, 'admin', now(), 'ACTIVE')


-- Ticket types
INSERT INTO ticket_types (id, type, price, quantity_available, event_id) VALUES
                                                                             (gen_random_uuid(), 'GENERAL',  280000, 5000, 'ef025378-2904-4311-9eb6-57911bc6ffcc'),
                                                                             (gen_random_uuid(), 'VIP',      650000, 800,  'ef025378-2904-4311-9eb6-57911bc6ffcc'),
                                                                             (gen_random_uuid(), 'STUDENT',  150000, 1200, 'ef025378-2904-4311-9eb6-57911bc6ffcc'),
                                                                             (gen_random_uuid(), 'GENERAL',  120000, 2000, '9dcf76b9-ca44-4192-b274-dd2d8b0afa2f'),
                                                                             (gen_random_uuid(), 'VIP',      250000, 300,  '9dcf76b9-ca44-4192-b274-dd2d8b0afa2f'),
                                                                             (gen_random_uuid(), 'STUDENT',   70000, 500,  '9dcf76b9-ca44-4192-b274-dd2d8b0afa2f'),
                                                                             (gen_random_uuid(), 'GENERAL',  200000, 800,  '5c2ee69b-6352-4af7-842d-6bfa30880013'),
                                                                             (gen_random_uuid(), 'VIP',      450000, 150,  '5c2ee69b-6352-4af7-842d-6bfa30880013'),
                                                                             (gen_random_uuid(), 'STUDENT',   90000, 400,  '5c2ee69b-6352-4af7-842d-6bfa30880013'),
                                                                             (gen_random_uuid(), 'GENERAL',   45000, 600,  '95051f33-3243-4c25-b43d-6fcee9dc150c'),
                                                                             (gen_random_uuid(), 'VIP',       90000, 100,  '95051f33-3243-4c25-b43d-6fcee9dc150c'),
                                                                             (gen_random_uuid(), 'STUDENT',   25000, 300,  '95051f33-3243-4c25-b43d-6fcee9dc150c')
