import os

base_dir = r'c:\formini\forminiProject_JavaVersion\src\main\resources\fxml\product'

for filename in os.listdir(base_dir):
    if filename.endswith('.fxml'):
        filepath = os.path.join(base_dir, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        new_content = content.replace('fx:controller="tn.formini.controllers.produits.', 'fx:controller="tn.formini.controllers.produit.')
        
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {filename}")
        else:
            print(f"No changes needed for {filename}")
