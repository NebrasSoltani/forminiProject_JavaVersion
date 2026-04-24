import re

input_path = r'c:\formini\forminiProject_JavaVersion\target\classes\css\style.css'
output_path = r'c:\formini\forminiProject_JavaVersion\src\main\resources\css\style.css'

with open(input_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

clean_lines = []
for line in lines:
    if not re.search(r'<<<<<<<|=======|>>>>>>>', line):
        clean_lines.append(line)

with open(output_path, 'w', encoding='utf-8') as f:
    f.writelines(clean_lines)

print("Restoration and cleanup of style.css complete.")
