#!/bin/bash

CURRENT_YEAR=$(date +"%Y")
LICENSE_HEADER='/*
 * Copyright (C) 2009-'"$CURRENT_YEAR"' by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */'

# Check what should be found in file
header="This file is part of geOrchestra."
extensions=(".java" ".js" ".ts" ".css" ".less" ".html")
exclude_dirs=("node_modules" "lib" "dist" "geonetwork" "geoserver" "geowebcache" ".mvn" "target" "build" "bin" "out" "vendor" "bootstrap*" )

# Build find command to locate files with specific extensions
find_command="find ."

# Add exclude directories
for dir in "${exclude_dirs[@]}"; do
  find_command+=" -not -path '*/$dir/*'"
done

# Add file extensions using -o (OR) operator
find_command+=" -type f \( -false"
for ext in "${extensions[@]}"; do
  find_command+=" -o -name \"*$ext\""
done
find_command+=" \)"

# Execute the find command and pipe results to grep
missing_header_files=$(eval "$find_command" | xargs grep -L "$header")

if [ -z "$missing_header_files" ]; then
  echo "All source files contain the header."
  exit 0
else
  file_count=$(echo "$missing_header_files" | wc -l)
  echo "The following files are missing the header ($file_count files):"
  echo "$missing_header_files"

  echo
  echo "Do you want to add the license to these files? (y/n)"
  read -r response

  if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo "Adding license to files..."

    while IFS= read -r file; do
      # Skip if file doesn't exist or is empty
      [ ! -f "$file" ] && continue

      echo "Processing $file"

      # Determine comment style based on file extension
      ext="${file##*.}"
      case "$ext" in
        java|js|ts|css|less)
          # Create temp file and add license header
          temp_file=$(mktemp)
          echo "$LICENSE_HEADER" > "$temp_file"
          echo "" >> "$temp_file"  # Add a blank line after the license
          cat "$file" >> "$temp_file"
          mv "$temp_file" "$file"
          ;;
        html)
          # For HTML files, we need to check if the file starts with <!DOCTYPE> or <html>
          # If it does, insert the license after it as an HTML comment
          temp_file=$(mktemp)
          if grep -q "<!DOCTYPE" "$file" || grep -q "<html" "$file"; then
            head_line=$(grep -n -m 1 -E '<!DOCTYPE|<html' "$file" | cut -d: -f1)
            head_content=$(head -n "$head_line" "$file")
            tail_content=$(tail -n +$((head_line + 1)) "$file")

            echo "$head_content" > "$temp_file"
            echo "<!--" >> "$temp_file"
            echo "$LICENSE_HEADER" | sed 's|/\*||' | sed 's|\*/||' >> "$temp_file"
            echo "-->" >> "$temp_file"
            echo "" >> "$temp_file"  # Add a blank line
            echo "$tail_content" >> "$temp_file"
          else
            echo "<!--" > "$temp_file"
            echo "$LICENSE_HEADER" | sed 's|/\*||' | sed 's|\*/||' >> "$temp_file"
            echo "-->" >> "$temp_file"
            echo "" >> "$temp_file"  # Add a blank line
            cat "$file" >> "$temp_file"
          fi
          mv "$temp_file" "$file"
          ;;
        *)
          echo "Skipping $file: unsupported file extension"
          continue
          ;;
      esac
    done <<< "$missing_header_files"

    echo "License added successfully!"
  else
    echo "Operation cancelled."
  fi
fi