#!/usr/bin/env python3
"""
QuranOxu - Automated Google Play Publisher Script
Uploads AAB, updates store listing texts, uploads 512x512 icon and feature graphic.
"""

import os
import sys
import json
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
import googleapiclient.errors

SCOPES = ['https://www.googleapis.com/auth/androidpublisher']
KEY_FILE = 'play-service-account.json'
PACKAGE_NAME = 'az.sananhaji.quranoxu'
AAB_PATH = 'app/build/outputs/bundle/release/app-release.aab'
ICON_PATH = 'store_assets/icon_512.png'
FEATURE_GRAPHIC_PATH = 'store_assets/feature_graphic.png'
LANGUAGE = 'az-AZ'

def main(track='internal'):
    print(f"🚀 Starting Google Play release automation for {PACKAGE_NAME} (Track: {track})...")
    
    if not os.path.exists(KEY_FILE):
        print(f"❌ Error: {KEY_FILE} not found!")
        sys.exit(1)
        
    if not os.path.exists(AAB_PATH):
        print(f"❌ Error: {AAB_PATH} not found! Run `./gradlew bundleRelease` first.")
        sys.exit(1)

    credentials = service_account.Credentials.from_service_account_file(
        KEY_FILE, scopes=SCOPES
    )
    service = build('androidpublisher', 'v3', credentials=credentials)
    print("✅ Successfully authenticated with Google Play Developer API.")

    # 1. Create a new edit
    try:
        edit_request = service.edits().insert(body={}, packageName=PACKAGE_NAME)
        edit = edit_request.execute()
        edit_id = edit['id']
        print(f"✅ Created Play Console Edit Session ID: {edit_id}")
    except googleapiclient.errors.HttpError as e:
        print(f"❌ Error creating edit: {e.resp.status}")
        print(e.content.decode('utf-8'))
        sys.exit(1)

    # 2. Upload the AAB bundle
    print(f"📦 Uploading AAB bundle ({os.path.getsize(AAB_PATH) / 1024 / 1024:.2f} MB)...")
    aab_media = MediaFileUpload(AAB_PATH, mimetype='application/octet-stream', resumable=True)
    bundle_upload = service.edits().bundles().upload(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        media_body=aab_media
    ).execute()
    version_code = bundle_upload['versionCode']
    print(f"✅ AAB uploaded successfully! Version Code: {version_code}")

    # 3. Assign to Track
    print(f"🎯 Assigning Version Code {version_code} to track '{track}'...")
    track_body = {
        'track': track,
        'releases': [{
            'versionCodes': [str(version_code)],
            'status': 'completed',
            'releaseNotes': [{
                'language': LANGUAGE,
                'text': 'QuranOxu ilk rəsmi buraxılış. Quran oxu, dinlə və öyrən.'
            }]
        }]
    }
    service.edits().tracks().update(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        track=track,
        body=track_body
    ).execute()
    print(f"✅ Track '{track}' configured.")

    # 4. Update Store Listing Texts
    print("📝 Updating Store Listing texts...")
    title = "Quran Oxu - Quran və Tərcüməsi"
    short_description = "Azərbaycan dilində Quran oxumaq, dinləmək və ayələri öyrənmək üçün tətbiq."
    
    # Read full description from store_assets/STORE_LISTING.md if available
    full_desc_file = 'store_assets/STORE_LISTING.md'
    full_description = (
        "QuranOxu – Müqəddəs Quranı Azərbaycan dilində oxumaq, anlamaq və qəlbləri riqqətə gətirən "
        "tilavətləri dinləmək üçün hazırlanmış müasir, zərif və tamamilə reklamsız mobil tətbiqdir.\n\n"
        "ƏSAS ÜSTÜNLÜKLƏR VƏ XÜSUSİYYƏTLƏR:\n"
        "• Müqəddəs Quranın tam 114 surəsi və 6236 ayəsi\n"
        "• Ərəbcə orijinal xətt və Azərbaycan dilində tərcümələr\n"
        "• Tanınmış Quran qarilərinin səsi ilə dinləmə və oflayn rejim\n"
        "• Planşet və dual-panel rejim dəstəyi\n"
        "• Əlfəcinlər və ayə qeydləri\n"
        "• Oxuma statistikası və gündəlik hədəflər\n"
        "• Zərif qaranlıq və işıqlı mövzu seçimi\n"
        "• Reklamsız və 100% məxfi."
    )

    try:
        service.edits().listings().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            language=LANGUAGE,
            body={
                'title': title,
                'shortDescription': short_description,
                'fullDescription': full_description
            }
        ).execute()
        print(f"✅ Store listing texts updated for '{LANGUAGE}'.")
    except Exception as e:
        print(f"⚠️ Warning updating listing texts: {e}")

    # 5. Upload Graphics (Icon & Feature Graphic)
    if os.path.exists(ICON_PATH):
        print("🎨 Uploading App Icon (512x512)...")
        try:
            icon_media = MediaFileUpload(ICON_PATH, mimetype='image/png')
            service.edits().images().upload(
                packageName=PACKAGE_NAME,
                editId=edit_id,
                language=LANGUAGE,
                imageType='icon',
                media_body=icon_media
            ).execute()
            print("✅ Icon uploaded successfully.")
        except Exception as e:
            print(f"⚠️ Warning uploading icon: {e}")

    if os.path.exists(FEATURE_GRAPHIC_PATH):
        print("🖼️ Uploading Feature Graphic (1024x500)...")
        try:
            fg_media = MediaFileUpload(FEATURE_GRAPHIC_PATH, mimetype='image/png')
            service.edits().images().upload(
                packageName=PACKAGE_NAME,
                editId=edit_id,
                language=LANGUAGE,
                imageType='featureGraphic',
                media_body=fg_media
            ).execute()
            print("✅ Feature Graphic uploaded successfully.")
        except Exception as e:
            print(f"⚠️ Warning uploading feature graphic: {e}")

    # 6. Commit the edit
    print("💾 Committing changes to Google Play Console...")
    commit_request = service.edits().commit(packageName=PACKAGE_NAME, editId=edit_id)
    commit_result = commit_request.execute()
    print("🎉 SUCCESS! Google Play release has been committed and published successfully!")
    print(commit_result)

if __name__ == '__main__':
    track = sys.argv[1] if len(sys.argv) > 1 else 'internal'
    main(track)
